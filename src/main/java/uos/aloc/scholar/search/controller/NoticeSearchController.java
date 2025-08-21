package uos.aloc.scholar.search.controller;

import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.dto.SearchResponseDTO;
import uos.aloc.scholar.search.service.NoticeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeSearchController {

    private final NoticeSearchService noticeSearchService;

    @GetMapping("/search")
    public SearchResponseDTO<NoticeResponseDTO> search(@ModelAttribute SearchRequestDTO req) {
    Page<NoticeResponseDTO> page = noticeSearchService.search(req);
    return SearchResponseDTO.<NoticeResponseDTO>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
}
}
