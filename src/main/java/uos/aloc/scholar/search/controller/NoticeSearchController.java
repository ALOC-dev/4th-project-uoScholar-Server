package uos.aloc.scholar.search.controller;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.dto.SearchResponseDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import uos.aloc.scholar.search.service.KeywordStatsService;
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
    private final NoticeSearchRepository noticeSearchRepository;
    private final KeywordStatsService keywordStatsService;

    @GetMapping("/search")
    public SearchResponseDTO<NoticeResponseDTO> search(@ModelAttribute SearchRequestDTO req) {

        // 1) 본문 목록 (기존 로직)
        Page<NoticeResponseDTO> page = noticeSearchService.search(req);

        // 2) HOT 조건: exact=true && page=0 && (keyword 비어있음)
        boolean wantHot = isHotRequested(req);

        // ✅ 키워드 저장 조건: page=0 이고, 카테고리가 전부 COLLEGE_* 이며,
        //    GENERAL/ACADEMIC 미포함, keyword 존재할 때만 카운트 증가
        if (shouldLogKeyword(req)) {
            keywordStatsService.log(req.getKeyword());
        }

        List<NoticeResponseDTO> hot = Collections.emptyList();
        if (wantHot) {
            List<NoticeCategory> cats = req.effectiveCategories(); // 프로젝트에 이미 존재한다고 가정
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
                .hot(hot)
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
        boolean exact = req.isExact();
        int page = Math.max(0, req.getPage());
        String keyword = req.normalizedKeyword();
        return exact && page == 0 && (keyword == null || keyword.isBlank());
    }

    // ===== 추가: 키워드 로깅 조건 검사 =====
    private boolean shouldLogKeyword(SearchRequestDTO req) {
        if (req == null) return false;
        if (req.getPage() != 0) return false;

        String kw = req.getKeyword();
        if (kw == null || kw.isBlank()) return false;

        List<NoticeCategory> cats = req.getCategory();
        if (cats == null || cats.isEmpty()) return false;

        // 모든 카테고리가 COLLEGE_* 이고, GENERAL/ACADEMIC이 하나도 없어야 함
        boolean allCollege = cats.stream().allMatch(this::isCollegeCategory);
        boolean hasGenOrAcad = cats.stream().anyMatch(this::isGeneralOrAcademic);

        return allCollege && !hasGenOrAcad;
    }

    private boolean isCollegeCategory(NoticeCategory c) {
        return c != null && c.name().startsWith("COLLEGE_");
    }

    private boolean isGeneralOrAcademic(NoticeCategory c) {
        return c == NoticeCategory.GENERAL || c == NoticeCategory.ACADEMIC;
    }
}
