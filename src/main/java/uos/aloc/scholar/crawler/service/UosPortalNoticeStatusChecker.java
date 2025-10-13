package uos.aloc.scholar.crawler.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import uos.aloc.scholar.crawler.util.UosPortalDomUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class UosPortalNoticeStatusChecker {

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 8000;

    public NoticeStatusResult check(String listId, int seq) {
        String url = buildViewUrl(listId, seq);
        try {
            Document doc = fetchWithWarmup(listId, seq);
            var eval = UosPortalDomUtils.evaluateH4(doc.selectFirst(".vw-tibx > h4"));
            return new NoticeStatusResult(listId, seq, eval.status(), null, eval.title(), url);
        } catch (Exception e) {
            return new NoticeStatusResult(listId, seq, "error", null, null, url);
        }
    }

    public NoticeStatusResult checkByUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return new NoticeStatusResult(null, -1, "error", null, null, null);
        }

        String url = normalizeUrl(rawUrl);
        try {
            if (isUosKorNoticeUrl(url)) {
                String listId = extractQueryParam(url, "list_id");
                if (listId == null) listId = extractQueryParam(url, "listId");
                Integer seq = tryParseInt(extractQueryParam(url, "seq"));

                if (listId != null && seq != null) {
                    Document doc = fetchWithWarmup(listId, seq);
                    var eval = UosPortalDomUtils.evaluateH4(doc.selectFirst(".vw-tibx > h4"));
                    return new NoticeStatusResult(listId, seq, eval.status(), null, eval.title(), url);
                }

                Document doc = fetchDirectWithMainWarm(url);
                var eval = UosPortalDomUtils.evaluateH4(doc.selectFirst(".vw-tibx > h4"));
                return new NoticeStatusResult(null, -1, eval.status(), null, eval.title(), url);
            } else {
                return new NoticeStatusResult(null, -1, "skipped", null, null, url);
            }
        } catch (Exception e) {
            return new NoticeStatusResult(null, -1, "error", null, null, url);
        }
    }

    public NoticeStatusResult checkWithRetry(String listId, int seq, int attempts, long sleepMillis) {
        NoticeStatusResult last = null;
        int tries = Math.max(1, attempts);
        for (int i = 0; i < tries; i++) {
            last = check(listId, seq);
            if (last != null && ("exists".equals(last.status()) || "deleted_or_unavailable".equals(last.status()))) {
                return last;
            }
            try {
                Thread.sleep(Math.max(0, sleepMillis));
            } catch (InterruptedException ignored) {}
        }
        return last;
    }

    public NoticeStatusResult checkWithRetryByUrl(String url, int attempts, long sleepMillis) {
        NoticeStatusResult last = null;
        int tries = Math.max(1, attempts);
        for (int i = 0; i < tries; i++) {
            last = checkByUrl(url);
            if (last != null && ("exists".equals(last.status()) || "deleted_or_unavailable".equals(last.status()) || "skipped".equals(last.status()))) {
                return last;
            }
            try {
                Thread.sleep(Math.max(0, sleepMillis));
            } catch (InterruptedException ignored) {}
        }
        return last;
    }

    public DebugResult debugFetchH4(String listId, int seq) {
        String url = buildViewUrl(listId, seq);
        try {
            Map<String, String> cookieJar = new HashMap<>();
            Connection.Response warm1 = Jsoup.connect("https://www.uos.ac.kr/main.do")
                    .userAgent(UA)
                    .referrer("https://www.uos.ac.kr/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Upgrade-Insecure-Requests", "1")
                    .timeout(TIMEOUT_MS)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .execute();
            cookieJar.putAll(warm1.cookies());

            String listUrl = buildListUrl(listId, 1);
            Connection.Response warm2 = Jsoup.connect(listUrl)
                    .userAgent(UA)
                    .referrer("https://www.uos.ac.kr/main.do")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Upgrade-Insecure-Requests", "1")
                    .timeout(TIMEOUT_MS)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .cookies(cookieJar)
                    .execute();
            cookieJar.putAll(warm2.cookies());

            String viewUrl = buildViewUrl(listId, seq);
            Connection conn = Jsoup.connect(viewUrl)
                    .userAgent(UA)
                    .referrer(listUrl)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Upgrade-Insecure-Requests", "1")
                    .timeout(TIMEOUT_MS)
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .cookies(cookieJar);

            Connection.Response resp = conn.execute();
            Document doc = resp.parse();

            Element h4 = doc.selectFirst(".vw-tibx > h4");
            String h4Html = h4 != null ? h4.html() : null;
            String h4Text = h4 != null ? h4.text() : null;
            String pageTitle = doc.selectFirst("title") != null ? doc.selectFirst("title").text() : null;

            boolean nbspTitle = h4Html != null && "&nbsp;".equals(h4Html.trim());

            return new DebugResult(
                    listId,
                    seq,
                    resp.statusCode(),
                    resp.url() != null ? resp.url().toString() : viewUrl,
                    UA,
                    h4Html,
                    h4Text,
                    pageTitle,
                    nbspTitle
            );
        } catch (Exception e) {
            return new DebugResult(listId, seq, -1, url, UA, null, null, null, false);
        }
    }

    private static String normalizeUrl(String raw) {
        String url = raw.trim();
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        if (url.startsWith("/")) {
            url = "https://www.uos.ac.kr" + url;
        }
        return url;
    }

    public static boolean isUosKorNoticeUrl(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost() == null ? "" : u.getHost().toLowerCase();
            String path = u.getPath() == null ? "" : u.getPath();
            return host.endsWith("uos.ac.kr") && path.contains("/korNotice/view.do");
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractQueryParam(String url, String key) {
        try {
            URI u = URI.create(url);
            String query = u.getRawQuery();
            if (query == null) return null;
            String[] parts = query.split("&");
            for (String p : parts) {
                int i = p.indexOf('=');
                String k = i >= 0 ? p.substring(0, i) : p;
                if (key.equalsIgnoreCase(k)) {
                    String v = i >= 0 ? p.substring(i + 1) : "";
                    return URLDecoder.decode(v, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer tryParseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Document fetchWithWarmup(String listId, int seq) throws Exception {
        Map<String, String> cookieJar = new HashMap<>();

        Connection.Response warm1 = Jsoup.connect("https://www.uos.ac.kr/main.do")
                .userAgent(UA)
                .referrer("https://www.uos.ac.kr/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .execute();
        cookieJar.putAll(warm1.cookies());

        String listUrl = buildListUrl(listId, 1);
        Connection.Response warm2 = Jsoup.connect(listUrl)
                .userAgent(UA)
                .referrer("https://www.uos.ac.kr/main.do")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .cookies(cookieJar)
                .execute();
        cookieJar.putAll(warm2.cookies());

        String viewUrl = buildViewUrl(listId, seq);
        Connection.Response resp = Jsoup.connect(viewUrl)
                .userAgent(UA)
                .referrer(listUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .cookies(cookieJar)
                .execute();

        return resp.parse();
    }

    private static Document fetchDirectWithMainWarm(String url) throws Exception {
        Map<String, String> cookieJar = new HashMap<>();
        Connection.Response warm1 = Jsoup.connect("https://www.uos.ac.kr/main.do")
                .userAgent(UA)
                .referrer("https://www.uos.ac.kr/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .execute();
        cookieJar.putAll(warm1.cookies());

        Connection.Response resp = Jsoup.connect(url)
                .userAgent(UA)
                .referrer("https://www.uos.ac.kr/main.do")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .cookies(cookieJar)
                .execute();
        return resp.parse();
    }

    private static String buildViewUrl(String listId, int seq) {
        StringBuilder sb = new StringBuilder("https://www.uos.ac.kr/korNotice/view.do");
        sb.append("?list_id=").append(listId)
          .append("&seq=").append(seq)
          .append("&pageIndex=1")
          .append("&viewAuth=Y")
          .append("&writeAuth=N");
        sb.append("&identified=anonymous");
        return sb.toString();
    }

    private static String buildListUrl(String listId, int pageIndex) {
        StringBuilder sb = new StringBuilder("https://www.uos.ac.kr/korNotice/list.do");
        sb.append("?list_id=").append(listId)
          .append("&seq=0&sort=&pageIndex=").append(pageIndex)
          .append("&searchCnd=&searchWrd=&cate_id=&viewAuth=Y&writeAuth=N")
          .append("&board_list_num=10&lpageCount=12");
        sb.append("&identified=anonymous");
        return sb.toString();
    }

    public record NoticeStatusResult(
            String listId,
            int seq,
            String status,
            String viewAuth,
            String title,
            String checkedUrl
    ) {}

    public record DebugResult(
            String listId,
            int seq,
            int httpStatus,
            String finalUrl,
            String userAgent,
            String h4Html,
            String h4Text,
            String pageTitle,
            boolean nbspTitle
    ) {}
}

