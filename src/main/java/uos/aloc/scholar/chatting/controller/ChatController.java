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
        // 프론트엔드로부터 받은 메시지를 가공하여 응답 메시지를 생성합니다.
        chatResponseDTO.setMessage("서버 응답입니다.");
        return chatResponseDTO;
    }
}
