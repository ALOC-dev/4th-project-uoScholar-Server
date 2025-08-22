package uos.aloc.scholar.search.controller;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.dto.SearchResponseDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import uos.aloc.scholar.search.service.NoticeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeSearchController {

    private final NoticeSearchService noticeSearchService;
    private final NoticeSearchRepository noticeSearchRepository; // ✅ 추가 주입

    @GetMapping("/search")
    public SearchResponseDTO<NoticeResponseDTO> search(@ModelAttribute SearchRequestDTO req) {

        // 1) 본문 목록 (기존 로직)
        Page<NoticeResponseDTO> page = noticeSearchService.search(req);

        // 2) HOT 조건: exact=true && page=0 && (keyword 비어있음)
        boolean wantHot = isHotRequested(req);

        List<NoticeResponseDTO> hot = Collections.emptyList();
        if (wantHot) {
            List<NoticeCategory> cats = req.effectiveCategories();
            List<Notice> hotEntities;
            if (cats.size() == 1) {
                hotEntities = noticeSearchRepository
                        .findTop3ByCategoryOrderByViewCountDescPostedDateDesc(cats.get(0));
            } else {
                hotEntities = noticeSearchRepository
                        .findTop3ByCategoryInOrderByViewCountDescPostedDateDesc(cats);
            }
            hot = hotEntities.stream().map(NoticeResponseDTO::from).toList();
        }

        // 3) 응답 빌드 (hot 포함)
        return SearchResponseDTO.<NoticeResponseDTO>builder()
                .hot(hot) // ✅ 추가
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private boolean isHotRequested(SearchRequestDTO req) {
        // exact, page, keyword 필드는 SearchRequestDTO에 이미 있는 전제.
        // 만약 exact가 없다면 boolean exact 추가 필요(아래 참고).
        boolean exact = req.isExact(); // getter 기준
        int page = Math.max(0, req.getPage());
        String keyword = req.normalizedKeyword();
        return exact && page == 0 && (keyword == null || keyword.isBlank());
    }
}
