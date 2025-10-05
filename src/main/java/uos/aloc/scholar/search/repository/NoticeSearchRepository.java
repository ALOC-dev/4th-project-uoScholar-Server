package uos.aloc.scholar.search.repository;

import uos.aloc.scholar.crawler.entity.*;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NoticeSearchRepository extends JpaRepository<Notice, Long> {

    @Query("""
        SELECT new uos.aloc.scholar.search.dto.NoticeResponseDTO(
                n.id,
                n.title,
                n.postedDate,
                n.department,
                n.link,
                n.category,
                n.viewCount
        )
          FROM Notice n
         WHERE n.category IN :categories
           AND (
                 :deptSize = 0
                 OR (n.department IS NOT NULL AND LOWER(n.department) IN :deptAliases)
               )
           AND (
                 :keyword IS NULL OR :keyword = ''
                 OR LOWER(n.title)      LIKE CONCAT('%', :keywordLower, '%')
                 OR LOWER(n.summary)    LIKE CONCAT('%', :keywordLower, '%')
                 OR LOWER(n.department) LIKE CONCAT('%', :keywordLower, '%')
               )
         ORDER BY n.postedDate DESC, n.id DESC
    """)
    Page<NoticeResponseDTO> search(@Param("keyword") String keyword,
                                   @Param("keywordLower") String keywordLower,
                                   @Param("categories") List<NoticeCategory> categories,
                                   @Param("deptAliases") List<String> deptAliases,
                                   @Param("deptSize") int deptSize,
                                   Pageable pageable);

    default Page<NoticeResponseDTO> search(String keyword,
                                           String keywordLower,
                                           List<NoticeCategory> categories,
                                           List<String> deptAliases,
                                           Pageable pageable) {
        List<String> safeAliases = (deptAliases == null || deptAliases.isEmpty())
                ? List.of()
                : List.copyOf(deptAliases);
        int deptSize = safeAliases.size();
        return search(keyword, keywordLower, categories, safeAliases, deptSize, pageable);
    }

    // ✅ HOT Top3 (단일 카테고리)
    List<Notice> findTop3ByCategoryAndPostedDateGreaterThanEqualOrderByViewCountDescPostedDateDesc(
            NoticeCategory category,
            LocalDate fromDate
    );

    // ✅ HOT Top3 (다중 카테고리 합집합에서 상위 3개)
    List<Notice> findTop3ByCategoryInAndPostedDateGreaterThanEqualOrderByViewCountDescPostedDateDesc(
            List<NoticeCategory> categories,
            LocalDate fromDate
    );
}
