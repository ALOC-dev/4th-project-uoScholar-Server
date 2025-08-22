package uos.aloc.scholar.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uos.aloc.scholar.search.service.KeywordStatsService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class KeywordStatsController {

    private final KeywordStatsService service;

    // 예: GET /search/popular?days=7&limit=5
    @GetMapping("/popular")
    public Map<String, String> popular(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<KeywordStatsService.PopularKeyword> list = service.top(days, limit);

        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            result.put("keyword" + (i + 1), list.get(i).keyword());
        }

        for (int i = list.size(); i < limit; i++) {
            result.put("keyword" + (i + 1), "");
        }
        return result;
    }
}
