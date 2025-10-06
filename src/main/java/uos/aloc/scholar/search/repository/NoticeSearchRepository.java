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
        SELECT n
        FROM Notice n
        WHERE n.category IN :categories
          AND ( :deptSize = 0 OR n.department IN :deptAliases )
          AND (
                :keyword IS NULL
             OR LOWER(n.title)      LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(COALESCE(n.department, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Notice> search(@Param("categories") List<NoticeCategory> categories,
                                   @Param("deptAliases") List<String> deptAliases,
                                   @Param("keyword") String keyword,
                                   @Param("deptSize") int deptSize,
                                   Pageable pageable);

    default Page<Notice> search(List<NoticeCategory> categories,
                                           List<String> deptAliases,
                                           String keyword,
                                           Pageable pageable) {
        int size = (deptAliases == null) ? 0 : deptAliases.size();
        return search(categories,
                (deptAliases == null) ? List.of() : deptAliases,
                keyword,
                size,
                pageable);
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
