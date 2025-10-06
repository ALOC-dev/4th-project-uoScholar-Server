package uos.aloc.scholar.search.controller;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.config.DepartmentFilterRegistry;
import uos.aloc.scholar.search.config.HotSearchProperties;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.dto.SearchResponseDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import uos.aloc.scholar.search.service.KeywordStatsService;
import uos.aloc.scholar.search.service.NoticeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeSearchController {

    private final NoticeSearchService noticeSearchService;
    private final DepartmentFilterRegistry deptRegistry;
    private final NoticeSearchRepository noticeSearchRepository;
    private final KeywordStatsService keywordStatsService;
    private final HotSearchProperties hotSearchProperties;
    private final Clock clock;

    @GetMapping("/search")
    public SearchResponseDTO<NoticeResponseDTO> search(@ModelAttribute SearchRequestDTO req) {

        final int page = normalizePage(req.getPage());
        final int size = normalizeSize(req.getSize());
        final Pageable pageable = PageRequest.of(page, size);

        // 1) 본문 목록 (기존 로직)
        Page<NoticeResponseDTO> result  = noticeSearchService.search(req, pageable);

        // 2) HOT 조건: exact=true && page=0 && (keyword 비어있음)
        boolean wantHot = isHotRequested(page, req.normalizedKeyword());

        // ✅ 키워드 저장 조건: page=0 이고, 카테고리가 전부 COLLEGE_* 이며,
        //    GENERAL/ACADEMIC 미포함, keyword 존재할 때만 카운트 증가
        if (shouldLogKeyword(req, page)) {
            keywordStatsService.log(req.normalizedKeyword());
        }

        // 2-2) HOT 조회
        List<NoticeResponseDTO> hot = Collections.emptyList();
        if (wantHot) {
            List<NoticeCategory> cats = req.effectiveCategories(deptRegistry);
            LocalDate fromDate = resolveHotFromDate();
            List<Notice> hotEntities;

            if (cats.size() == 1) {
                hotEntities = noticeSearchRepository
                        .findTop3ByCategoryAndPostedDateGreaterThanEqualOrderByViewCountDescPostedDateDesc(
                                cats.get(0), fromDate
                        );
            } else {
                hotEntities = noticeSearchRepository
                        .findTop3ByCategoryInAndPostedDateGreaterThanEqualOrderByViewCountDescPostedDateDesc(
                                cats, fromDate
                        );
            }
            hot = hotEntities.stream().map(NoticeResponseDTO::from).toList();
        }

        // 3) 응답 빌드 (hot 포함)
        return SearchResponseDTO.<NoticeResponseDTO>builder()
                .hot(hot)
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    // 부가 함수들 

    private int normalizePage(Integer page) {
        if (page == null) return 0;
        return Math.max(0, page);
    }

    private int normalizeSize(Integer size) {
        if (size == null) return 15;
        // 필요 시 상한선(예: 100) 적용 가능
        return Math.max(1, size);
    }

    private boolean isHotRequested(int page, String normalizedKeyword) {
        return page == 0 && (normalizedKeyword == null || normalizedKeyword.isBlank());
    }

    /** page==0 + keyword 존재 + 카테고리 해석 결과가 모두 COLLEGE_* 이고 GENERAL/ACADEMIC 미포함 여기 수정해야됨 */
    private boolean shouldLogKeyword(SearchRequestDTO req, int page) {
        if (req == null) return false;
        if (page != 0) return false;

        String kw = req.normalizedKeyword();
        return kw != null && !kw.isBlank();
    }

    private LocalDate resolveHotFromDate() {
        long days = hotSearchProperties.getLookbackDays();
        LocalDate today = LocalDate.now(clock);
        if (days <= 0) {
            return today;
        }
        return today.minusDays(days);
    }
}
