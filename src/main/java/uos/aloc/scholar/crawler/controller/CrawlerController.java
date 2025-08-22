package uos.aloc.scholar.crawler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uos.aloc.scholar.crawler.service.UosViewCountCrawler;

@RestController
@RequestMapping("/admin/view-sync")
@RequiredArgsConstructor
public class CrawlerController {

    private final UosViewCountCrawler crawler;

    // http://localhost:8080/admin/view-sync/all?pages=3
    @PostMapping("/all")
    public String syncAll(@RequestParam(defaultValue = "10") int pages) {
        crawler.syncAllBoards(pages);
        return "OK";
    }

    // http://localhost:8080/admin/view-sync/general?pages=3
    @PostMapping("/general")
    public String syncGeneral(@RequestParam(defaultValue = "10") int pages) {
        crawler.syncCategoryWithListId(
            uos.aloc.scholar.crawler.entity.NoticeCategory.GENERAL,
            "FA1",
            pages
        );
        return "OK";
    }
}
