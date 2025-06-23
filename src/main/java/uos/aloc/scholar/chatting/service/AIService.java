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
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uos.aloc.scholar.chatting.dto.NoticeDTO;

@Service
@RequiredArgsConstructor
public class AIService {
    private final RestTemplate restTemplate;
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.server.url:https://5000-alocdev-3rdprojectuosch-3ihm59nyj5t.ws-us120.gitpod.io}")
    private String aiServerUrl; // AI 서버 URL

    public List<NoticeDTO> getAIResponse(String message) { 
        // 요청 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("user_input", message);

        List<NoticeDTO> aiResponse = null;
        try {
            String response = restTemplate.postForObject(aiServerUrl + "/search", requestPayload, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode resultsNode = root.path("results"); // <== "results" 필드 접근
            aiResponse = mapper.readValue(resultsNode.traverse(), new TypeReference<List<NoticeDTO>>() {});
        } catch (Exception e) {
            logger.error("AI 서버 호출 중 오류 발생: {}", e.getMessage(), e);
        }   
        
        return aiResponse;
    }
}
