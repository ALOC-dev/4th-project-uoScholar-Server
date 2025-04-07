package uos.aloc.scholar.chatting.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {
    private final RestTemplate restTemplate;
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.server.url:http://localhost:5000}")
    private String aiServerUrl; // AI 서버 URL

    public String getAIResponse(String message) {
        // 요청 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("message", message);

        String aiResponse = null;
        try {
            // AI 서버에 요청 및 응답 받기
            aiResponse = restTemplate.postForObject(aiServerUrl + "/get-response", requestPayload, String.class);
        } catch (RestClientException e) {
            logger.error("AI 서버 호출 중 오류 발생: {}", e.getMessage(), e);
            aiResponse = "AI 서버 호출 중 오류가 발생했습니다.";
        }
        return aiResponse;
    }
}
