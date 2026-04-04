
package com.yiguardsilverfa.service;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dto.VoiceResponse;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VoiceService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private HealthQaDAO healthQaDAO;

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private ElderInfoDAO elderInfoDAO;
    
    @Value("${baidu.asr.api-key}")
    private String baiduAsrApiKey;
    
    @Value("${baidu.asr.secret-key}")
    private String baiduAsrSecretKey;
    
    @Value("${baidu.tts.api-key}")
    private String baiduTtsApiKey;
    
    @Value("${baidu.tts.secret-key}")
    private String baiduTtsSecretKey;
    
    @Value("${rag.service.url:http://localhost:5000/chat}")
    private String ragServiceUrl;
    
    private String baiduAccessToken;
    private long tokenExpireTime;
    
    /**
     * 处理语音问答
     */
    public VoiceResponse processVoiceChat(MultipartFile audioFile, Long userId) {
        VoiceResponse response = new VoiceResponse();
        
        try {
            // 保存临时音频文件
            Path tempFile = saveTempAudio(audioFile);
            
            // 百度语音识别（ASR）- 方言转文字
            String text = baiduSpeechToText(tempFile);
            response.setText(text);
            log.info("识别结果: {}", text);
            
            // 调用RAG服务获取回答
            String answer = callRagService(text, userId);
            response.setAnswer(answer);
            log.info("RAG回答: {}", answer);
            
            // 保存问答记录
            saveQaRecord(userId, text, answer);
            
            // 百度语音合成（TTS）- 文字转语音
            byte[] audioData = baiduTextToSpeech(answer);
            
            // 返回音频（base64格式）
            String audioBase64 = Base64.getEncoder().encodeToString(audioData);
            response.setAudioBase64(audioBase64);
            
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            
        } catch (Exception e) {
            log.error("语音处理失败: {}", e.getMessage(), e);
            response.setText("识别失败");
            response.setAnswer("抱歉，我没有听清楚，能再说一遍吗？");
            // 生成默认错误提示音频
            try {
                byte[] errorAudio = baiduTextToSpeech(response.getAnswer());
                response.setAudioBase64(Base64.getEncoder().encodeToString(errorAudio));
            } catch (Exception ex) {
                log.error("生成错误音频失败", ex);
            }
        }
        
        return response;
    }
    
    /**
     * 文字问答
     */
    public String processTextChat(String question, Long userId) {
        try {
            // 添加空判断
            if (question == null || question.trim().isEmpty()) {
                String defaultAnswer = "您好，请问有什么可以帮您？";
                saveQaRecord(userId, "用户没有输入问题", defaultAnswer);
                return defaultAnswer;
            }

            String answer = callRagService(question, userId);
            saveQaRecord(userId, question, answer);
            return answer;
        } catch (Exception e) {
            log.error("文字问答失败: {}", e.getMessage());
            String errorAnswer = "抱歉，系统暂时无法回答您的问题，请稍后再试。";
            saveQaRecord(userId, question != null ? question : "未知问题", errorAnswer);
            return errorAnswer;
        }
    }
    
    /**
     * 百度语音识别（支持方言）
     */
    private String baiduSpeechToText(Path audioFile) throws Exception {
        String accessToken = getBaiduAccessToken();
        
        // 读取音频文件
        byte[] audioData = Files.readAllBytes(audioFile);
        String audioBase64 = Base64.getEncoder().encodeToString(audioData);
        
        // 构建请求
        String url = "https://vop.baidu.com/server_api";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("format", "pcm");  // 音频格式
        requestBody.put("rate", 16000);     // 采样率
        requestBody.put("channel", 1);      // 单声道
        requestBody.put("token", accessToken);
        requestBody.put("cuid", "yifasilverguard");
        requestBody.put("len", audioData.length);
        requestBody.put("speech", audioBase64);
        requestBody.put("dev_pid", 15376);  // 多方言自动识别（粤语/四川话/东北话等）
        
        // 发送请求
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(new com.google.gson.Gson().toJson(requestBody).getBytes());
            os.flush();
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        // 解析结果
        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject();
        if (json.get("err_no").getAsInt() == 0) {
            return json.getAsJsonArray("result").get(0).getAsString();
        } else {
            throw new RuntimeException("ASR识别失败: " + json.get("err_msg").getAsString());
        }
    }
    
    /**
     * 百度语音合成
     */
    private byte[] baiduTextToSpeech(String text) throws Exception {
        String accessToken = getBaiduAccessToken();
        
        // 构建请求
        String url = "https://tsn.baidu.com/text2audio?tok=" + accessToken +
                "&tex=" + java.net.URLEncoder.encode(text, "UTF-8") +
                "&per=4" +      // 音色：4-情感女声
                "&spd=3" +      // 语速：3-偏慢
                "&pit=8" +      // 音调：8-稍高
                "&vol=9" +      // 音量：9-较大
                "&aue=6" +      // 返回格式：6-pcm
                "&cuid=yifasilverguard";
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        
        // 读取音频数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = conn.getInputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        
        byte[] audioData = baos.toByteArray();
        
        // 检查是否为错误响应
        if (audioData.length > 0 && audioData[0] == '{') {
            String error = new String(audioData);
            throw new RuntimeException("TTS合成失败: " + error);
        }
        
        return audioData;
    }
    
    /**
     * 获取百度AccessToken
     */
    private String getBaiduAccessToken() throws Exception {
        // 如果token还有效，直接返回
        if (baiduAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return baiduAccessToken;
        }
        
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials" +
                "&client_id=" + baiduAsrApiKey +
                "&client_secret=" + baiduAsrSecretKey;
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject();
        baiduAccessToken = json.get("access_token").getAsString();
        int expiresIn = json.get("expires_in").getAsInt();
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
        
        return baiduAccessToken;
    }
    
    /**
     * 保存临时音频文件
     */
    private Path saveTempAudio(MultipartFile audioFile) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path tempFile = Paths.get(tempDir, "voice_" + System.currentTimeMillis() + ".pcm");
        Files.write(tempFile, audioFile.getBytes());
        return tempFile;
    }
    
    /**
     * 调用RAG服务
     */
    private String callRagService(String question, Long userId) {
        try {
            // 添加空判断
            if (question == null || question.trim().isEmpty()) {
                return "您好，请问有什么可以帮您？";
            }

            User user = loginDAO.selectUserById(userId);
            ElderInfo elderInfo = elderInfoDAO.selectElderInfoByUserId(userId);

            Map<String, Object> request = new HashMap<>();
            request.put("question", question);
            request.put("user_id", userId);
            if (elderInfo != null) {
                request.put("elder_info", elderInfo);
            }

            log.info("调用RAG服务，URL: {}, 问题: {}", ragServiceUrl, question);

            // 发送HTTP请求到RAG服务
            Map<String, String> response = restTemplate.postForObject(
                    ragServiceUrl,
                    request,
                    Map.class
            );

            if (response != null && response.containsKey("answer") && response.get("answer") != null) {
                return response.get("answer");
            }
            return getDefaultAnswer(question);

        } catch (Exception e) {
            log.error("RAG服务调用失败: {}", e.getMessage());
            return getDefaultAnswer(question);
        }
    }
    
    /**
     * 保存问答记录
     */
    private void saveQaRecord(Long userId, String question, String answer) {
        try {
            // 确保 question 不为 null
            if (question == null) {
                question = "空问题";
            }
            if (answer == null) {
                answer = "暂无回答";
            }

            HealthQa qa = new HealthQa();
            qa.setElderId(userId);
            qa.setQuestion(question);
            qa.setAnswer(answer);
            qa.setAskTime(LocalDateTime.now());
            qa.setIsEmergency(isEmergencyQuestion(question) ? 1 : 0);
            healthQaDAO.insert(qa);
            log.info("问答记录已保存");
        } catch (Exception e) {
            log.error("保存问答记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 判断是否为紧急问题
     */
    private boolean isEmergencyQuestion(String text) {
        if (text == null) {
            return false;
        }

        String[] keywords = {"救命", "不舒服", "摔倒", "急救", "难受", "晕倒", "心慌", "胸闷"};
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 默认回答（当RAG服务不可用时）
     */
    private String getDefaultAnswer(String question) {
        if (question == null) {
            return "您好，我是您的健康助手。请问有什么可以帮您？";
        }

        String q = question.toLowerCase();
        if (q.contains("血压")) {
            return "建议您每天早晚各测一次血压，记录下来。如果持续偏高或偏低，请及时咨询医生。";
        } else if (q.contains("药") || q.contains("吃药")) {
            return "提醒您按时吃药，记得饭后半小时服用效果更好。";
        } else if (q.contains("天气")) {
            return "今天天气不错，注意保暖。";
        } else if (q.contains("时间") || q.contains("几点")) {
            return "现在是" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } else if (q.contains("你好") || q.contains("您好")) {
            return "您好！我是您的健康助手，有什么可以帮您的吗？";
        }

        return "您说的是：" + question + "，我记下了。";
    }
}