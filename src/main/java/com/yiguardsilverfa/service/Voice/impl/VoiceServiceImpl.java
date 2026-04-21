package com.yiguardsilverfa.service.Voice.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiguardsilverfa.config.WarningWebSocketHandler;
import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.dao.WarningEventDAO;
import com.yiguardsilverfa.dto.VoiceResponse;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.entity.WarningEvent;
import com.yiguardsilverfa.service.LLM.LLMService;
import com.yiguardsilverfa.service.Voice.VoiceService;
import com.yiguardsilverfa.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class VoiceServiceImpl implements VoiceService {

    @Autowired
    private HealthQaDAO healthQaDAO;

    @Autowired
    private ElderInfoDAO elderInfoDAO;

    @Autowired
    private LLMService llmService;

    @Autowired
    private WebSocketUtil webSocketUtil;

    @Autowired
    private WarningEventDAO warningEventDAO;

    @Autowired
    private WarningWebSocketHandler warningWebSocketHandler;

    // 定义紧急关键词
    private static final List<String> EMERGENCY_KEYWORDS = Arrays.asList(
            "胸痛", "晕倒", "呼吸困难", "大出血", "救命", "120", "昏厥", "心梗", "中风", "意识不清"
    );

    @Override
    public String processTextChat(String question, Long userId) {
        if (question == null || question.isBlank()) {
            return "您好！请问有什么可以帮您？";
        }
        String elderInfo = getAllElderInfoAsString(userId);
        String ans = llmService.generateAnswer(question, elderInfo);
        //判断是否紧急（根据问题或回答关键词）
        boolean isEmergency = checkEmergency(question, ans);
        int emergencyFlag = isEmergency ? 1 : 0;
        saveRecord(userId, question, ans,emergencyFlag);
        //如果是紧急情况，创建预警并推送
        if (isEmergency) {
            createWarningAndPush(userId, question);
        }
        return ans;
    }

    // 支持多个老人
    private String getAllElderInfoAsString(Long userId) {
        try {
            List<ElderInfo> elderList = elderInfoDAO.selectElderInfoByUserId(userId);
            if (elderList == null || elderList.isEmpty()) return "未绑定老人信息";

            StringBuilder sb = new StringBuilder();
            sb.append("家属账号下共有 ").append(elderList.size()).append(" 位老人：");

            for (int i = 0; i < elderList.size(); i++) {
                ElderInfo elder = elderList.get(i);
                sb.append("\n第").append(i + 1).append("位：")
                        .append("年龄").append(elder.getAge()).append("岁，")
                        .append("病史：").append(elder.getMedicalHistory() == null ? "无" : elder.getMedicalHistory()).append("，")
                        .append("过敏史：").append(elder.getAllergy() == null ? "无" : elder.getAllergy());
            }
            return sb.toString();
        } catch (Exception e) {
            return "获取老人信息失败";
        }
    }

    private void saveRecord(Long userId, String q, String a, int isEmergency) {
        try {
            HealthQa qa = new HealthQa();
            qa.setElderId(userId);
            qa.setQuestion(q);
            qa.setAnswer(a);
            qa.setIsEmergency(isEmergency);
            qa.setAskTime(LocalDateTime.now());
            healthQaDAO.insert(qa);
        } catch (Exception e) {
            log.error("保存失败", e);
        }
    }

    /**
     * 判断问题或回答是否紧急
     */
    private boolean checkEmergency(String question, String answer) {
        String text=(question+" "+answer).toLowerCase();
        for (String kw : EMERGENCY_KEYWORDS) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 创建预警事件并推送 WebSocket 消息
     */
    private void createWarningAndPush(Long elderId, String question) {
        try {
            // 插入预警记录
            WarningEvent event = new WarningEvent();
            event.setElderId(elderId);
            event.setType(1);                     // 1-紧急问答
            event.setContent("老人紧急提问：" + question);
            event.setLocation("未知");             // 可根据设备获取位置
            event.setHandleStatus(0);             // 未处理
            warningEventDAO.insert(event);

            // 2. 查询老人姓名
            String elderName = elderInfoDAO.selectElderInfoByUserId(elderId).get(0).getName();

            // 构造推送消息（JSON 格式）
            Map<String, Object> message = new HashMap<>();
            message.put("type", "emergency_qa");
            message.put("eventId", event.getId());
            message.put("elderId", elderId);
            message.put("elderName", elderName);
            message.put("content", question);
            message.put("time", LocalDateTime.now().toString());
            String jsonMsg = new ObjectMapper().writeValueAsString(message);

            // 通过 WebSocket 推送给所有已连接的家属端
            warningWebSocketHandler.sendWarningToAll(jsonMsg);
        } catch (Exception e) {
            log.error("创建预警或推送失败", e);
        }
    }

}