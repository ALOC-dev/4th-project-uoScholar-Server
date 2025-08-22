package uos.aloc.scholar.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uos.aloc.scholar.search.service.KeywordStatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class KeywordStatsController {

    private final KeywordStatsService service;

    // 예: GET /search/popular?days=7&limit=5
    @GetMapping("/popular")
    public List<KeywordStatsService.PopularKeyword> popular(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.top(days, limit);
    }
}
