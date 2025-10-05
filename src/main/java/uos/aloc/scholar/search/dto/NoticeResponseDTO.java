package uos.aloc.scholar.search.dto;

import uos.aloc.scholar.crawler.entity.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponseDTO {
    private Long id;
    private String title;
    private LocalDate postedDate;
    private String department;
    private String link;
    private String category; // 영문 enum 그대로
    private Integer viewCount;

    public static NoticeResponseDTO from(Notice n) {
        return NoticeResponseDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .postedDate(n.getPostedDate())
                .department(n.getDepartment())
                .link(n.getLink())
                .category(n.getCategory() != null ? n.getCategory().name() : null)
                .viewCount(n.getViewCount())
                .build();
    }

    public NoticeResponseDTO(Long id,
                             String title,
                             LocalDate postedDate,
                             String department,
                             String link,
                             NoticeCategory category,
                             Integer viewCount) {
        this(id,
                title,
                postedDate,
                department,
                link,
                category != null ? category.name() : null,
                viewCount);
    }
}
