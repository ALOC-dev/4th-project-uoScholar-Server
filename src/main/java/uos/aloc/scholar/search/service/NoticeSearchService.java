package uos.aloc.scholar.search.service;

import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeSearchService {
    private final NoticeSearchRepository repository;

    public NoticeSearchService(NoticeSearchRepository repository) {
        this.repository = repository;
    }

    /**
     * 제목에 keyword가 포함된 공지 조회
     */
    public List<Notice> searchByTitle(String keyword) {
        return repository.findByTitleContaining(keyword);
    }
}
