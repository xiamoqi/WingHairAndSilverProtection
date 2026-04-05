package com.yiguardsilverfa.service.Voice;

import com.yiguardsilverfa.dto.VoiceResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {
    VoiceResponse processVoiceChat(MultipartFile audioFile, Long userId);
    String processTextChat(String question, Long userId);
}