package uos.aloc.scholar.search.repository;

import uos.aloc.scholar.crawler.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeSearchRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByTitleContaining(String keyword);
}
