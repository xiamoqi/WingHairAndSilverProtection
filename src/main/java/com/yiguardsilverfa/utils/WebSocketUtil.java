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