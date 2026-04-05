package com.yiguardsilverfa.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Slf4j
public class WebSocketUtil {

    @Value("${baidu.speech.api-key}")
    private String apiKey;

    @Value("${baidu.speech.secret-key}")
    private String secretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String cachedAccessToken;
    private long tokenExpireTime;

    // 语音识别
    public String recognizeVoice(byte[] audioData) {
        try {
            String token = getToken();
            if (token == null) return "识别失败";

            JsonObject body = new JsonObject();
            body.addProperty("format", "pcm");
            body.addProperty("rate", 16000);
            body.addProperty("channel", 1);
            body.addProperty("token", token);
            body.addProperty("cuid", "java_app");
            body.addProperty("len", audioData.length);
            body.addProperty("speech", java.util.Base64.getEncoder().encodeToString(audioData));
            body.addProperty("dev_pid", 15376);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://vop.baidu.com/server_api"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

            if (json.get("err_no").getAsInt() == 0) {
                return json.getAsJsonArray("result").get(0).getAsString();
            }
        } catch (Exception e) {
            log.error("识别失败", e);
        }
        return "识别失败";
    }

    // 语音合成
    public byte[] synthesizeSpeech(String text) {
        try {
            String token = getToken();
            if (token == null) return new byte[0];

            String url = "https://tsn.baidu.com/text2audio?tok=" + token
                    + "&tex=" + java.net.URLEncoder.encode(text, "UTF-8")
                    + "&per=4&spd=3&pit=5&vol=9&aue=6";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return resp.body();
        } catch (Exception e) {
            log.error("合成失败", e);
            return new byte[0];
        }
    }

    // 获取TOKEN
    private String getToken() {
        try {
            if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
                return cachedAccessToken;
            }

            String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials"
                    + "&client_id=" + apiKey
                    + "&client_secret=" + secretKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

            cachedAccessToken = json.get("access_token").getAsString();
            tokenExpireTime = System.currentTimeMillis() + 259200000L;
            return cachedAccessToken;
        } catch (Exception e) {
            log.error("获取TOKEN失败", e);
            return null;
        }
    }
}