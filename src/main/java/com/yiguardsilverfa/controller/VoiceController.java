package com.yiguardsilverfa.controller;

import com.yiguardsilverfa.dto.TextChatRequest;
import com.yiguardsilverfa.dto.VoiceResponse;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.service.Voice.VoiceService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    @Autowired
    private VoiceService voiceService;

    @PostMapping("/chat")
    public Result<?> voiceChat(@RequestParam("audio") MultipartFile audioFile) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        // 处理语音问答
        VoiceResponse response = voiceService.processVoiceChat(audioFile, userId);
        return Result.success(response);
    }
    
    /**
     * 文字问答接口
     */
    @PostMapping("/chat/text")
    public Result<?> textChat(@RequestBody TextChatRequest request) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        String answer = voiceService.processTextChat(request.getQuestion(), userId);
        return Result.success(answer);
    }
}