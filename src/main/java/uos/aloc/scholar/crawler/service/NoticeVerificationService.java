package uos.aloc.scholar.crawler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.crawler.repository.NoticeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeVerificationService {

    private static final int MAX_AGE_DAYS = 240;

    private final NoticeRepository noticeRepository;
    private final UosPortalNoticeStatusChecker portalChecker;

    public NoticeStatusResult checkById(Long id) {
        return noticeRepository.findById(id)
                .map(this::checkNotice)
                .orElseGet(() -> new NoticeStatusResult(null, -1, "error", null, null));
    }

    public NoticeStatusResult checkByUrl(String url) {
        UosPortalNoticeStatusChecker.NoticeStatusResult res = portalChecker.checkWithRetryByUrl(url, 2, 120);
        return new NoticeStatusResult(res.listId(), res.seq(), res.status(), res.title(), res.checkedUrl());
    }

    public DeletedScanResponse scanDeletedNotices(String categoryParam) {
        List<Notice> notices = selectNotices(categoryParam);
        LocalDate threshold = LocalDate.now().minusDays(MAX_AGE_DAYS);

        int scanned = 0;
        int deleted = 0;
        int unknown = 0;
        List<DeletedItem> items = new ArrayList<>();

        for (Notice notice : notices) {
            if (notice.getPostedDate() != null && notice.getPostedDate().isBefore(threshold)) {
                continue;
            }

            String link = notice.getLink();
            if (link == null || link.isBlank() || !UosPortalNoticeStatusChecker.isUosKorNoticeUrl(link)) {
                continue;
            }

            UosPortalNoticeStatusChecker.NoticeStatusResult res = portalChecker.checkWithRetryByUrl(link, 2, 120);
            scanned++;

            if ("deleted_or_unavailable".equals(res.status())) {
                deleted++;
                items.add(new DeletedItem(
                        notice.getId(),
                        notice.getCategory().name(),
                        notice.getPostNumber(),
                        res.listId(),
                        res.status(),
                        res.title(),
                        res.checkedUrl()
                ));
            } else if ("unknown".equals(res.status()) || "error".equals(res.status())) {
                unknown++;
            }

            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
        }

        return new DeletedScanResponse(scanned, deleted, unknown, notices.size(), items);
    }

    private List<Notice> selectNotices(String categoryParam) {
        if (categoryParam == null || categoryParam.isBlank()) {
            return noticeRepository.findAll();
        }
        try {
            NoticeCategory category = NoticeCategory.valueOf(categoryParam.trim());
            return noticeRepository.findByCategory(category);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private NoticeStatusResult checkNotice(Notice notice) {
        String link = notice.getLink();
        if (link == null || link.isBlank()) {
            return new NoticeStatusResult(null, -1, "error", null, null);
        }
        UosPortalNoticeStatusChecker.NoticeStatusResult res = portalChecker.checkWithRetryByUrl(link, 2, 120);
        return new NoticeStatusResult(res.listId(), res.seq(), res.status(), res.title(), res.checkedUrl());
    }

    public record NoticeStatusResult(
            String listId,
            int seq,
            String status,
            String title,
            String checkedUrl
    ) {}

    public record DeletedItem(
            Long id,
            String category,
            Integer postNumber,
            String listId,
            String status,
            String title,
            String checkedUrl
    ) {}

    public record DeletedScanResponse(
            int scanned,
            int deletedCount,
            int unknownCount,
            long totalCandidates,
            List<DeletedItem> items
    ) {}
}
