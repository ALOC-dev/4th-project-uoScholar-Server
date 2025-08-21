package uos.aloc.scholar.search.repository;

import uos.aloc.scholar.crawler.entity.*;          // ⚠ 실제 패키지로 교체
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
}
