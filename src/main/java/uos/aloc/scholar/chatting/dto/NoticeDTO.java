package uos.aloc.scholar.chatting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeDTO {
    private String title;
    private String department;
    private String link;
    private String posted_date;

    @Override
    public String toString() {
        return String.format("날짜: %s, 작성자: %s, 제목: %s, 링크: %s", posted_date, department, title, link);
    }
}


