package uos.aloc.scholar.crawler.service;

import lombok.RequiredArgsConstructor;
import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.crawler.repository.NoticeRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UosViewCountCrawler {

    private final NoticeRepository noticeRepository;

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final int TIMEOUT_MS = 8000;
    private static final int MISS_STREAK_LIMIT = 40;

    private static final Pattern FNVIEW_SEQ = Pattern.compile("fnView\\s*\\('.*?'\\s*,\\s*'([0-9]+)'\\s*\\)");
    private static final Pattern URL_SEQ    = Pattern.compile("[?&]seq=([0-9]+)");

    /* =========================
       PUBLIC API
       ========================= */

    /** 지정된 4개 보드를 순차 실행 */
    @Transactional
    public void syncAllBoards(int maxPages) {
        syncCategoryWithListId(NoticeCategory.GENERAL,   "FA1",      maxPages); 
        syncCategoryWithListId(NoticeCategory.ACADEMIC,  "FA2",      maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_ENGINEERING,"20013DA1", maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_HUMANITIES,"human01",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_SOCIAL_SCIENCES,"econo01",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_URBAN_SCIENCE,"urbansciences01",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_ARTS_SPORTS,"artandsport01",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_BUSINESS,"20008N2",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_NATURAL_SCIENCES,"scien01",  maxPages); 
        syncCategoryWithListId(NoticeCategory.COLLEGE_LIBERAL_CONVERGENCE,"clacds01",  maxPages); 
    }

    /** 기존 단일진행 메서드(FA1 전용)*/
    @Transactional
    public void syncGeneralFA1(int maxPages) {
        syncCategoryWithListId(NoticeCategory.GENERAL, "FA1", maxPages);
    }

    /** 공통 로직: list_id만 바꾸면 다른 보드에도 적용 가능 */
    @Transactional
    public void syncCategoryWithListId(NoticeCategory category, String listId, int maxPages) {
        int missStreak = 0;

        for (int pageIndex = 1; pageIndex <= maxPages; pageIndex++) {
            String url = buildUrl(listId, pageIndex);
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(UA)
                        .referrer("https://www.uos.ac.kr/")
                        .timeout(TIMEOUT_MS)
                        .get();

                // li 중에서 <p class="num">가 있는 항목만
                Elements lis = doc.select("li:has(p.num)");
                List<ViewEntry> entries = new ArrayList<>();
                for (Element li : lis) {
                    Element pNum = li.selectFirst("p.num");
                    if (pNum == null) continue;

                    String numText = pNum.text().trim();
                    // "공지" 같은 비숫자 제외
                    if (!numText.matches("\\d+")) continue;

                    Long seq = extractSeq(li);
                    if (seq == null) continue;

                    Integer views = extractViews(li);
                    if (views == null) continue;

                    try {
                        entries.add(new ViewEntry(Math.toIntExact(seq), views));
                    } catch (ArithmeticException ignore) {
                        // 게시글 번호가 int 범위를 벗어나는 경우는 건너뛴다.
                    }
                }

                missStreak = applyEntries(category, entries, missStreak);
                if (missStreak >= MISS_STREAK_LIMIT) {
                    System.out.printf("[ViewSync] %s(listId=%s) miss %d in a row → stop (page=%d)%n",
                            category, listId, MISS_STREAK_LIMIT, pageIndex);
                    return;
                }

                try { Thread.sleep(120); } catch (InterruptedException ignored) {}

            } catch (Exception e) {
                System.err.printf("[ViewSync] %s(listId=%s) page %d failed: %s%n",
                        category, listId, pageIndex, e.getMessage());
                // 페이지 실패는 넘어가되 다음 페이지 시도
            }
        }
    }

    /* =========================
       내부 유틸
       ========================= */

    private static String buildUrl(String listId, int pageIndex) {
        return "https://www.uos.ac.kr/korNotice/list.do"
         + "?list_id=" + listId
         + "&seq=0&sort=&pageIndex=" + pageIndex
         + "&searchCnd=&searchWrd=&cate_id=&viewAuth=Y&writeAuth=N"
         + "&board_list_num=10&lpageCount=12"
         + "&menuid=2000005009002000000";
    }

    /**
     * seq(post_number) 추출:
     *  1) <a href="javascript:fnView('4','29450');"> 에서 29450
     *  2) 첨부 링크 등 "/...seq=29450" 에서 29450
     */
    private static Long extractSeq(Element li) {
        // 1) fnView 패턴 우선
        for (Element a : li.select("a[href]")) {
            String href = a.attr("href");
            Long s = tryParseSeq(href);
            if (s != null) return s;
        }
        // 2) onclick 속성
        String onclick = li.attr("onclick");
        Long s = tryParseSeq(onclick);
        if (s != null) return s;

        // 3) 첨부 a 태그의 href에서 seq 파라미터
        for (Element a : li.select("a[href*='seq=']")) {
            Long s2 = tryParseSeq(a.attr("href"));
            if (s2 != null) return s2;
        }
        return null;
    }

    private static Long tryParseSeq(String s) {
        if (s == null) return null;

        Matcher m1 = FNVIEW_SEQ.matcher(s);
        if (m1.find()) {
            try { return Long.parseLong(m1.group(1)); } catch (Exception ignored) {}
        }
        Matcher m2 = URL_SEQ.matcher(s);
        if (m2.find()) {
            try { return Long.parseLong(m2.group(1)); } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * 조회수 추출: <div class="da"><span>부서</span><span>날짜</span><span>조회수</span>...</div>
     * 일반적으로 마지막 span이 조회수.
     */
    private static Integer extractViews(Element li) {
        Element da = li.selectFirst("div.da");
        if (da == null) return null;

        Elements spans = da.select("> span");
        if (spans.isEmpty()) return null;

        String text = spans.get(spans.size() - 1).text();
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;

        try { return Integer.parseInt(digits); } catch (Exception e) { return null; }
    }

    int applyEntries(NoticeCategory category, List<ViewEntry> entries, int currentMissStreak) {
        if (entries == null || entries.isEmpty()) {
            return currentMissStreak;
        }

        List<Integer> postNumbers = entries.stream().map(ViewEntry::postNumber).distinct().toList();
        List<Notice> notices = noticeRepository.findByCategoryAndPostNumberIn(category, postNumbers);
        Map<Integer, Integer> existingViews = new HashMap<>();
        for (Notice notice : notices) {
            if (notice.getPostNumber() == null) continue;
            existingViews.put(notice.getPostNumber(), notice.getViewCount());
        }

        int missStreak = currentMissStreak;
        for (ViewEntry entry : entries) {
            Integer currentView = existingViews.get(entry.postNumber());
            if (currentView == null) {
                missStreak++;
            } else {
                missStreak = 0;
                if (!Objects.equals(currentView, entry.viewCount())) {
                    noticeRepository.updateViewCount(category, entry.postNumber(), entry.viewCount());
                }
            }

            if (missStreak >= MISS_STREAK_LIMIT) {
                break;
            }
        }

        return missStreak;
    }

    record ViewEntry(int postNumber, int viewCount) { }
}
