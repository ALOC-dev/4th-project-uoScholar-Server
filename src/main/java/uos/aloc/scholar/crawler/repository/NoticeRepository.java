package uos.aloc.scholar.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.Collection;
import java.util.List;

@Repository
// NoticeRepository.java
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      UPDATE Notice n
         SET n.viewCount = :viewCount
       WHERE n.category = :category AND n.postNumber = :postNumber
    """)
    int updateViewCount(@Param("category") NoticeCategory category,
                        @Param("postNumber") Integer postNumber,
                        @Param("viewCount") Integer viewCount);

    List<Notice> findByCategoryAndPostNumberIn(NoticeCategory category, Collection<Integer> postNumbers);
}

