
package com.yiguardsilverfa.service;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dto.VoiceResponse;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.entity.User;
import com.yiguardsilverfa.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private LLMService llmService;

    @Autowired
    private WebSocketUtil webSocketUtil;

    @Value("${baidu.asr.api-key}")
    private String baiduAsrApiKey;

    @Value("${baidu.asr.secret-key}")
    private String baiduAsrSecretKey;

    @Value("${baidu.tts.api-key}")
    private String baiduTtsApiKey;

    @Value("${baidu.tts.secret-key}")
    private String baiduTtsSecretKey;

    @Value("${baidu.speech.api-key:}")
    private String baiduApiKey;

    @Value("${baidu.speech.secret-key:}")
    private String baiduSecretKey;

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
            // 读取音频数据
            byte[] audioData = audioFile.getBytes();

            // 用于存储识别结果
            StringBuilder recognizedText = new StringBuilder();
            CompletableFuture<String> asrFuture = new CompletableFuture<>();

            // 创建 ASR 连接
            okhttp3.WebSocket asrWs = webSocketUtil.createRealtimeAsr(
                    text -> {
                        // 实时识别结果
                        recognizedText.append(text);
                        asrFuture.complete(text);
                    },
                    error -> {
                        log.error("ASR错误: {}", error);
                        asrFuture.completeExceptionally(new RuntimeException(error));
                    }
            );

            if (asrWs == null) {
                throw new RuntimeException("ASR连接失败");
            }

            // 发送音频数据
            int chunkSize = 3200;  // 100ms 一帧（16kHz * 2字节 * 0.1秒）
            for (int offset = 0; offset < audioData.length; offset += chunkSize) {
                int end = Math.min(offset + chunkSize, audioData.length);
                byte[] chunk = Arrays.copyOfRange(audioData, offset, end);
                boolean isLast = (end == audioData.length);
                webSocketUtil.sendAudioToAsr(asrWs, chunk, isLast);

                // 实时发送延迟
                Thread.sleep(100);
            }

            // 等待识别结果
            String text = asrFuture.get(10, java.util.concurrent.TimeUnit.SECONDS);
            response.setText(text);
            log.info("最终识别结果: {}", text);

            // 关闭 ASR 连接
            webSocketUtil.closeAsr(asrWs);

            // 调用 RAG + 大模型
            String answer = callRagService(text, userId);
            response.setAnswer(answer);
            log.info("AI回答: {}", answer);

            // 保存问答记录
            saveQaRecord(userId, text, answer);

            // 流式语音合成
            ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
            CompletableFuture<Void> ttsFuture = new CompletableFuture<>();

            okhttp3.WebSocket ttsWs = webSocketUtil.createStreamingTts(
                    answer,
                    audioChunk -> {
                        // 实时收到音频数据
                        try {
                            audioStream.write(audioChunk);
                        } catch (IOException e) {
                            log.error("写入音频失败", e);
                        }
                    },
                    () -> ttsFuture.complete(null),
                    error -> ttsFuture.completeExceptionally(new RuntimeException(error))
            );

            if (ttsWs == null) {
                throw new RuntimeException("TTS连接失败");
            }

            // 等待合成完成
            ttsFuture.get(30, java.util.concurrent.TimeUnit.SECONDS);

            // 返回音频 Base64
            byte[] audioDataOut = audioStream.toByteArray();
            response.setAudioBase64(Base64.getEncoder().encodeToString(audioDataOut));

        } catch (Exception e) {
            log.error("语音处理失败: {}", e.getMessage(), e);
            response.setText("识别失败");
            response.setAnswer("抱歉，我没有听清楚，能再说一遍吗？");
            // 降级使用 HTTP TTS
            try {
                byte[] fallbackAudio = fallbackTextToSpeech(response.getAnswer());
                response.setAudioBase64(Base64.getEncoder().encodeToString(fallbackAudio));
            } catch (Exception ex) {
                log.error("降级TTS失败", ex);
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
     * 降级 HTTP TTS
     */
    private byte[] fallbackTextToSpeech(String text) throws Exception {
        String token = getBaiduAccessToken();
        String url = "https://tsn.baidu.com/text2audio?tok=" + token +
                "&tex=" + java.net.URLEncoder.encode(text, "UTF-8") +
                "&per=4&spd=3&pit=8&vol=9&aue=6&cuid=yifasilverguard";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            return response.body().bytes();
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

            StringBuilder context = new StringBuilder();
            if (elderInfo != null) {
                context.append("老人信息：");
                if (elderInfo.getAge() != null) context.append(elderInfo.getAge()).append("岁；");
                if (elderInfo.getMedicalHistory() != null) context.append("既往病史：").append(elderInfo.getMedicalHistory()).append("；");
                if (elderInfo.getAllergy() != null) context.append("过敏史：").append(elderInfo.getAllergy()).append("；");
            }

            String answer = llmService.generateAnswer(question, context.toString());
            return answer;

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
            if (question == null) question = "空问题";
            if (answer == null) answer = "暂无回答";

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
        if (text == null) return false;
        String[] keywords = {"救命", "不舒服", "摔倒", "急救", "难受", "晕倒", "心慌", "胸闷", "胸痛"};
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 默认回答
     */
    private String getDefaultAnswer(String question) {
        if (question == null) return "您好，请问有什么可以帮您？";

        String q = question.toLowerCase();
        if (q.contains("血压")) {
            return "建议您每天早晚各测一次血压，记录下来。如果持续偏高或偏低，请及时咨询医生。";
        } else if (q.contains("药") || q.contains("吃药")) {
            return "提醒您按时吃药，记得饭后半小时服用效果更好。";
        } else if (q.contains("你好") || q.contains("您好")) {
            return "您好！我是您的健康助手，有什么可以帮您的吗？";
        }
        return "您说的是：" + question + "，我记下了。";
    }
}