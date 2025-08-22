package uos.aloc.scholar.search.repository;

import uos.aloc.scholar.search.entity.SearchKeywordDaily;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

public interface KeywordStatsRepository extends JpaRepository<SearchKeywordDaily, Long> {

    Optional<SearchKeywordDaily> findBykeywordAndDay(String keyword, LocalDate day);

    // 최근 N일 Top K 집계
    @Query("""
        SELECT k.keyword AS keyword, SUM(k.count) AS total
          FROM SearchKeywordDaily k
         WHERE k.day >= :from
         GROUP BY k.keyword
         ORDER BY SUM(k.count) DESC
        """)
    List<Object[]> topFrom(@Param("from") LocalDate from);
}
