package uos.aloc.scholar.crawler.controller;

import uos.aloc.scholar.crawler.service.CrawlerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawl")
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    // 수동으로 일정 seqNumber에서부터 가장 최근 것까지 crawl실행.
    @GetMapping("/all")
    public String crawlNotices() {
        crawlerService.crawlNotices();
        return "크롤링 완료";
    }
}
