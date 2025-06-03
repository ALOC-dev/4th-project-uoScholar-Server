package uos.aloc.scholar.chatting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uos.aloc.scholar.chatting.dto.ChatRequestDTO;
import uos.aloc.scholar.chatting.dto.ChatResponseDTO;
import uos.aloc.scholar.chatting.dto.NoticeDTO;
import uos.aloc.scholar.chatting.service.AIService;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AIService aiService;

    public ChatController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ai")
    public ChatResponseDTO ai(@RequestBody ChatRequestDTO chatRequestDTO) {
    
        List<NoticeDTO> aiResponse = aiService.getAIResponse(chatRequestDTO.getMessage());

        ChatResponseDTO chatResponseDTO = new ChatResponseDTO();
        chatResponseDTO.setMessage(aiResponse);

        return chatResponseDTO;
    }

    // @PostMapping("/test")
    // public ChatResponseDTO test(@RequestBody ChatRequestDTO chatRequestDTO) {
    //     ChatResponseDTO chatResponseDTO = new ChatResponseDTO();
    //     chatResponseDTO.setMessage("서버 응답입니다.");
    //     return chatResponseDTO;
    // }
}
