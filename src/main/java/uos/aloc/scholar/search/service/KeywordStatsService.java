// src/main/java/uos/aloc/scholar/search/service/KeywordStatsService.java
package uos.aloc.scholar.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.aloc.scholar.search.entity.SearchKeywordDaily;
import uos.aloc.scholar.search.repository.KeywordStatsRepository;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeywordStatsService {

    private final KeywordStatsRepository repo;

    private static final int MIN_LEN = 2; // 1글자 방지
    private static final Set<String> STOPWORDS = Set.of("공지", "학사", "공지사항", "및", "그리고");

    /** 검색 키워드 기록 (요청당 1회) */
    @Transactional
    public void log(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword.isBlank() || keyword.length() < MIN_LEN || STOPWORDS.contains(keyword)) return;

        LocalDate today = LocalDate.now();

        // 낙관적 처리: 없으면 생성, 있으면 +1
        try {
            var row = repo.findBykeywordAndDay(keyword, today).orElseGet(() ->
                    SearchKeywordDaily.builder().keyword(keyword).day(today).count(0).build()
            );
            row.inc(); // +1
            repo.save(row);
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 인한 유니크 충돌 방어: 재조회 후 +1
            var row = repo.findBykeywordAndDay(keyword, today).orElseThrow();
            row.inc();
            repo.save(row);
        }
    }

    /** 최근 N일 Top K */
    @Transactional(readOnly = true)
    public List<PopularKeyword> top(int days, int limit) {
        int d = Math.max(0, days); // 0=오늘
        int l = Math.min(Math.max(1, limit), 50);
        LocalDate from = LocalDate.now().minusDays(d);

        return repo.topFrom(from).stream()
                .map(r -> new PopularKeyword((String) r[0], ((Number) r[1]).longValue()))
                .limit(l)
                .collect(Collectors.toList());
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String x = s.trim().replaceAll("\\s+", " ");
        x = Normalizer.normalize(x, Normalizer.Form.NFC);
        return x.toLowerCase(Locale.ROOT);
    }

    public record PopularKeyword(String keyword, long count) {}
}
