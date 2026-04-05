package com.yiguardsilverfa.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okio.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.WebSocket;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class WebSocketUtil {

    @Value("${baidu.speech.app-id:}")
    private String appId;

    @Value("${baidu.speech.api-key:}")
    private String apiKey;

    @Value("${baidu.speech.secret-key:}")
    private String secretKey;

    private String cachedAccessToken;
    private long tokenExpireTime;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(5, TimeUnit.SECONDS)
            .build();

    private String buildAsrWebSocketUrl() throws Exception {
        String token = getBaiduAccessToken(apiKey, secretKey);
        String cuid = "yiguardsilverfa";
        // 百度实时语音识别WebSocket地址
        return String.format("wss://vop.baidu.com/realtime_asr?token=%s&cuid=%s&dev_pid=15376&sample_rate=16000&format=pcm",
                token, cuid);
    }
    // 百度语音实时识别
    public WebSocket createRealtimeAsr(Consumer<String> onResult, Consumer<String> onError) {
        try {
            String url = buildAsrWebSocketUrl();
            log.info("连接实时语音识别：{}", url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            WebSocketListener listener = new WebSocketListener() {

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    log.info("ASR WebSocket连接成功");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    try {
                        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                        if (json.has("err_no") && json.get("err_no").getAsInt() != 0) {
                            String errMsg = json.has("err_msg") ? json.get("err_msg").getAsString() : "未知错误";
                            log.error("ASR 错误: err_no={}, err_msg={}", json.get("err_no").getAsInt(), errMsg);
                            onError.accept(errMsg);
                            return;
                        }
                        if (json.has("result")) {
                            JsonObject result = json.getAsJsonObject("result");
                            if (result.has("final_text")) {
                                text = result.get("final_text").getAsString();
                                if (text != null && !text.isEmpty()) {
                                    log.info("ASR 识别结果: {}", text);
                                    onResult.accept(text);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析ASR结果失败: {}", e.getMessage());
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.error("ASR WebSocket 失败: {}", t.getMessage());
                    onError.accept(t.getMessage());
                }
            };
            return client.newWebSocket(request, listener);

        } catch (Exception e) {
            log.error("创建ASR连接失败: {}", e.getMessage());
            onError.accept(e.getMessage());
            return null;
        }
    }

    // 发送音频到ASR
    public void sendAudioToAsr(WebSocket webSocket, byte[] audioData, boolean isLast) {
        if (webSocket == null) return;
        // 构建二进制音频帧
        ByteString frame = buildAudioFrame(audioData, isLast);
        webSocket.send(frame);

        if (isLast) {
            log.info("发送最后一帧音频");
        }
    }

    // 关闭ASR连接
    public void closeAsr(WebSocket webSocket) {
        if (webSocket != null) {
            webSocket.close(1000, "normal");
        }
    }

    // 流式语音合成连接
    public WebSocket createStreamingTts(String text, Consumer<byte[]> onAudio, Runnable onComplete, Consumer<String> onError) {
        try {
            String url = buildTtsWebSocketUrl();
            log.info("连接流式TTS: {}", url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    log.info("TTS WebSocket 连接成功");
                    // 发送合成请求
                    sendTtsRequest(webSocket, text);
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    // 收到音频数据
                    byte[] audioData = bytes.toByteArray();
                    if (audioData.length > 0) {
                        onAudio.accept(audioData);
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    // 收到文本消息
                    try {
                        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                        if (json.has("err_no") && json.get("err_no").getAsInt() != 0) {
                            String errMsg = json.has("err_msg") ? json.get("err_msg").getAsString() : "未知错误";
                            onError.accept(errMsg);
                        } else if (json.has("status") && json.get("status").getAsInt() == 2) {
                            // 合成完成
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        log.error("解析TTS消息失败: {}", e.getMessage());
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.error("TTS WebSocket 失败: {}", t.getMessage());
                    onError.accept(t.getMessage());
                }
            };

            return client.newWebSocket(request, listener);

        } catch (Exception e) {
            log.error("创建TTS连接失败: {}", e.getMessage());
            onError.accept(e.getMessage());
            return null;
        }
    }

    // 构建ASR的URL
    private String buildTtsWebSocketUrl() throws Exception {
        String token = getBaiduAccessToken(apiKey, secretKey);
        String cuid = "yiguardsilverfa";

        String per = "4189";
        String spd = "3";   // 语速
        String pit = "8";   // 音调
        String vol = "9";   // 音量
        return String.format("wss://tsn.baidu.com/text2audio?tok=%s&cuid=%s&per=%s&spd=%s&pit=%s&vol=%s&aue=6",
                token, cuid, per, spd, pit, vol);
    }

    // 发送TTS请求
    private void sendTtsRequest(WebSocket webSocket, String text) {
        try {
            // 构建请求消息
            JsonObject request = new JsonObject();
            request.addProperty("type", "TEXT");
            request.addProperty("data", text);

            webSocket.send(request.toString());
            log.info("发送TTS合成请求: {}", text);

        } catch (Exception e) {
            log.error("发送TTS请求失败: {}", e.getMessage());
        }
    }

    // 构建音频数据帧
    private ByteString buildAudioFrame(byte[] audioData, boolean isLast) {
        byte[] frame = new byte[4 + audioData.length];
        frame[0] = 0x01;
        frame[1] = 0x00;
        frame[2] = 0x00;
        frame[3] = 0x00;
        System.arraycopy(audioData, 0, frame, 4, audioData.length);

        return ByteString.of(frame);
    }

    // 获取百度语音识别的AccessToken
    private String getBaiduAccessToken(String apiKey, String secretKey) throws Exception {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }

        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials" +
                "&client_id=" + apiKey + "&client_secret=" + secretKey;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0]))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("获取百度AccessToken失败: " + response.code());
            }
            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String token = json.get("access_token").getAsString();
            long expiresIn = json.get("expires_in").getAsLong();

            this.cachedAccessToken = token;
            // 提前60秒过期，确保安全
            this.tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000;

            return token;
        }
    }
}
