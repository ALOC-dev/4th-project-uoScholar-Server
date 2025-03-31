package uos.aloc.scholar.crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uos.aloc.scholar.crawler.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 게시글번호가 이미 존재하는지 확인하기 위한 메소드
    boolean existsByPostNumber(Integer postNumber);
}
