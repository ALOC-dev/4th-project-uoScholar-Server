package uos.aloc.scholar.crawler.util;

import org.jsoup.nodes.Element;

public final class UosPortalDomUtils {

    private UosPortalDomUtils() {}

    public static Status evaluateH4(Element h4) {
        if (h4 == null) {
            return new Status("unknown", null);
        }
        String innerHtml = h4.html() == null ? "" : h4.html().trim();
        String text = h4.text() == null ? "" : h4.text().trim();

        if ("&nbsp;".equals(innerHtml)) {
            return new Status("deleted_or_unavailable", null);
        }
        if (!text.isEmpty()) {
            return new Status("exists", text);
        }
        return new Status("unknown", null);
    }

    public record Status(String status, String title) {}
}

