package uos.aloc.scholar.crawler.controller;

import uos.aloc.scholar.crawler.service.CrawlerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    // Postman 등에서 GET 요청 시 크롤링 수행
    @GetMapping("/api/crawl")
    public String crawlNotices() {
        crawlerService.crawlNotices();
        return "크롤링 완료";
    }
}
