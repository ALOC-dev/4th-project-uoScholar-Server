package uos.aloc.scholar.crawler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uos.aloc.scholar.crawler.service.UosViewCountCrawler;
import uos.aloc.scholar.crawler.service.NoticeVerificationService;

@RestController
@RequestMapping("/admin/view-sync")
@RequiredArgsConstructor
public class CrawlerController {

    private final UosViewCountCrawler crawler;
    private final NoticeVerificationService verificationService;

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


    // Test API (by DB id): looks up Notice by id and checks using URL (portal links retry)
    // Example: GET /admin/view-sync/notice-status-by-id?id=1234
    @GetMapping("/notice-status-by-id")
    public NoticeVerificationService.NoticeStatusResult noticeStatusById(
            @RequestParam Long id
    ) {
        return verificationService.checkById(id);
    }

    // Scan DB notices and return only deleted/unavailable ones
    // Example: GET /admin/view-sync/deleted-notices
    //          GET /admin/view-sync/deleted-notices?category=GENERAL
    @GetMapping("/deleted-notices")
    public NoticeVerificationService.DeletedScanResponse listDeletedNotices(
            @RequestParam(required = false) String category
    ) {
        return verificationService.scanDeletedNotices(category);
    }
}
