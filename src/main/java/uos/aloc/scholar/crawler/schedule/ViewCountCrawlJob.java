package uos.aloc.scholar.crawler.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uos.aloc.scholar.crawler.service.UosViewCountCrawler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountCrawlJob {

    private final UosViewCountCrawler crawler;

    // 몇 페이지까지 훑을지 (기본 10)
    @Value("${crawler.view-sync.pages:10}")
    private int pages;

    // 기본: 3시간마다
    @Scheduled(cron = "${crawler.view-sync.cron:0 0 */3 * * *}", zone = "Asia/Seoul")
    public void run() {
        log.info("[ViewCountCrawlJob] start (pages={})", pages);
        try {
            crawler.syncAllBoards(pages);
            log.info("[ViewCountCrawlJob] done");
        } catch (Exception e) {
            log.error("[ViewCountCrawlJob] failed", e);
        }
    }
}
