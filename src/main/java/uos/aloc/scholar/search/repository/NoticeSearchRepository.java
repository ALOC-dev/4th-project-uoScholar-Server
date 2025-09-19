package uos.aloc.scholar.search.repository;

import uos.aloc.scholar.crawler.entity.*;        
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
           AND (
                 :keyword IS NULL OR :keyword = ''
                 OR n.title      LIKE CONCAT(CONCAT('%', :keyword, '%'))
                 OR n.summary    LIKE CONCAT(CONCAT('%', :keyword, '%'))
                 OR n.department LIKE CONCAT(CONCAT('%', :keyword, '%'))
               )
         ORDER BY n.postedDate DESC, n.id DESC
    """)
    Page<Notice> search(@Param("keyword") String keyword,
                        @Param("categories") List<NoticeCategory> categories,
                        Pageable pageable);

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
