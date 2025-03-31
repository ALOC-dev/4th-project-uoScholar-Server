package uos.aloc.scholar.chatting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uos.aloc.scholar.chatting.dto.ChatRequestDTO;
import uos.aloc.scholar.chatting.dto.ChatResponseDTO;

@RestController
public class ChatController {

    @PostMapping("/chat")
    public ChatResponseDTO chat(@RequestBody ChatRequestDTO chatRequestDTO) {
        ChatResponseDTO chatResponseDTO = new ChatResponseDTO();
        chatResponseDTO.setMessage("서버 응답입니다.");
        return chatResponseDTO;
    }
}
