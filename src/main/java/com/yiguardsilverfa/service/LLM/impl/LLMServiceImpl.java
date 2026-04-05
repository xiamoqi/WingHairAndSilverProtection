package com.yiguardsilverfa.service.LLM.impl;

import com.yiguardsilverfa.service.LLM.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LLMServiceImpl implements LLMService {

    @Value("${qianfan.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateAnswer(String question, String context) {
        String prompt = buildPrompt(question, context);
        return callQianfan(prompt);
    }

    private String callQianfan(String prompt) {
        try {
            String url = "https://qianfan.baidubce.com/v2/chat/completions";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "ernie-speed-pro-128k");
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用千帆 API, API Key: {}...", apiKey.substring(0, Math.min(20, apiKey.length())));

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                // 解析 choices[0].message.content
                if (body.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        if (message != null && message.containsKey("content")) {
                            return (String) message.get("content");
                        }
                    }
                }
            }

            log.warn("大模型返回异常: {}", response.getBody());
            return getFallbackAnswer(prompt);

        } catch (Exception e) {
            log.error("千帆大模型调用失败: {}", e.getMessage(), e);
            return getFallbackAnswer(prompt);
        }
    }

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

    private String getFallbackAnswer(String question) {
        if (question == null) return "您好，请问有什么可以帮您？";
        String q = question.toLowerCase();
        if (q.contains("血压")) {
            return "建议您每天早晚各测一次血压，记录下来。如果持续偏高或偏低，请及时咨询医生。";
        } else if (q.contains("药") || q.contains("吃药")) {
            return "提醒您按时吃药，记得饭后半小时服用效果更好。";
        } else if (q.contains("你好") || q.contains("您好")) {
            return "您好！我是您的健康助手，有什么可以帮您的吗？";
        } else if (q.contains("胸口") || q.contains("闷") || q.contains("不舒服")) {
            return "听到您不舒服，建议您先坐下休息。如果症状持续或加重，请及时就医或拨打120。";
        }
        return "您说的是：" + question + "，我记下了。";
    }
}