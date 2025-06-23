package uos.aloc.scholar.chatting.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    public String getPosted_date() {
        try {
            return ZonedDateTime
                    .parse(posted_date, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // 파싱 실패 시, 원본을 그대로 내려줌
            return posted_date;
        }
    }
}


