package uos.aloc.scholar.search.service;

import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NoticeSearchServiceImpl implements NoticeSearchService {

    private final NoticeSearchRepository noticeSearchRepository;
    private final DepartmentFilterRegistry departmentFilterRegistry;

    @Override
    public Page<NoticeResponseDTO> search(SearchRequestDTO req) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, req.getPage()),
                Math.max(1, req.getSize())
        );

        String keyword = req.normalizedKeyword();
        String keywordLower = keyword.toLowerCase(Locale.ROOT);
        List<NoticeCategory> categories = req.effectiveCategories();
        List<String> deptAliases = req.departmentAliases(departmentFilterRegistry);

        return noticeSearchRepository
                .search(keyword, keywordLower, categories, deptAliases, pageable);
    }
}
