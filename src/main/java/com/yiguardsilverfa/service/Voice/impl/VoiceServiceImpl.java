package com.yiguardsilverfa.service.Voice.impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.dto.VoiceResponse;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.service.LLM.LLMService;
import com.yiguardsilverfa.service.Voice.VoiceService;
import com.yiguardsilverfa.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

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


    @Override
    public String processTextChat(String question, Long userId) {
        if (question == null || question.isBlank()) {
            return "您好！请问有什么可以帮您？";
        }
        String elderInfo = getAllElderInfoAsString(userId);
        String ans = llmService.generateAnswer(question, elderInfo);
        saveRecord(userId, question, ans);
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

    private void saveRecord(Long userId, String q, String a) {
        try {
            HealthQa qa = new HealthQa();
            qa.setElderId(userId);
            qa.setQuestion(q);
            qa.setAnswer(a);
            qa.setAskTime(LocalDateTime.now());
            healthQaDAO.insert(qa);
        } catch (Exception e) {
            log.error("保存失败", e);
        }
    }
}