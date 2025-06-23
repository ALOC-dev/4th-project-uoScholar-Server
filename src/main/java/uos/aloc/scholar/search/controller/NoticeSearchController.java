package uos.aloc.scholar.search.controller;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.service.NoticeSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class NoticeSearchController {

    private final NoticeSearchService service;

    public NoticeSearchController(NoticeSearchService service) {
        this.service = service;
    }

    /**
     * POST /search
     * Content-Type: application/json
     * Body: { "search": "계절학기" }
     */
    @PostMapping
    public ResponseEntity<?> searchNotices(@RequestBody SearchRequestDTO request) {
        String keyword = request.getSearch();
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        java.util.Map.of("error", "검색어(search)는 필수입니다.")
                    );
        }

        List<Notice> found = service.searchByTitle(keyword);
        if (found.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        java.util.Map.of("error", "검색 결과가 없습니다.")
                    );
        }

        List<NoticeResponseDTO> results = found.stream()
            .map(n -> {
                NoticeResponseDTO dto = new NoticeResponseDTO();
                dto.setTitle(n.getTitle());
                dto.setDepartment(n.getDepartment());
                dto.setLink(n.getLink());
                dto.setPosted_date(n.getPostedDate().toString());
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(
            java.util.Map.of("results", results)
        );
    }
}
