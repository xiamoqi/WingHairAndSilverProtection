package com.yiguardsilverfa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class LLMService {

    @Value("${llm.provider:qianfan}")
    private String provider;

    @Value("${qianfan.api.key:}")
    private String qianfanApiKey;

    @Value("${qianfan.secret.key:}")
    private String qianfanSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 缓存 AccessToken
    private String cachedAccessToken;
    private long tokenExpireTime;

    /**
     * 调用大模型生成回答
     */
    public String generateAnswer(String question, String context) {
        String prompt = buildPrompt(question, context);
        
        if ("qianfan".equals(provider)) {
            return callQianfan(prompt);
        }
        
        return getFallbackAnswer(question);
    }

    /**
     * 百度文心一言 API 调用
     */
    private String callQianfan(String prompt) {
        try {
            String accessToken = getQianfanAccessToken();
            String apiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k?access_token=" + accessToken;
            
            Map<String, Object> request = new HashMap<>();
            request.put("messages", List.of(
                Map.of("role", "system", "content", "你是专业的老年健康助手，请用通俗易懂、温暖的语言回答老人的健康问题。"),
                Map.of("role", "user", "content", prompt)
            ));
            request.put("temperature", 0.7);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            
            if (response != null && response.containsKey("result")) {
                return (String) response.get("result");
            }
            return getFallbackAnswer(prompt);
            
        } catch (Exception e) {
            log.error("文心一言调用失败: {}", e.getMessage());
            return getFallbackAnswer(prompt);
        }
    }
    
    /**
     * 获取文心一言 AccessToken
     */
    private String getQianfanAccessToken() throws Exception {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }
        
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials" +
                "&client_id=" + qianfanApiKey + "&client_secret=" + qianfanSecretKey;
        
        Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
        cachedAccessToken = (String) response.get("access_token");
        int expiresIn = (int) response.get("expires_in");
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
        
        return cachedAccessToken;
    }

    /**
     * 构建 RAG 提示词
     */
    private String buildPrompt(String question, String context) {
        if (context == null || context.isEmpty()) {
            return String.format("请回答老人的健康问题：%s", question);
        }
        
        return String.format("""
            请基于以下【健康档案】信息回答老人的问题。
            
            【健康档案】
            %s
            
            【老人问题】
            %s
            
            【回答要求】
            1. 用通俗易懂、温暖的语言
            2. 如涉及紧急情况，提醒立即就医
            3. 回答控制在100字以内
            """, context, question);
    }

    /**
     * 兜底回答
     */
    private String getFallbackAnswer(String question) {
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