package uos.aloc.scholar.chatting.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChatResponseDTO {
    private List<NoticeDTO> message; // 백엔드 응답 메시지 
}
