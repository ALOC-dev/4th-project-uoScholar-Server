package uos.aloc.scholar.search.service;

import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeSearchServiceImpl implements NoticeSearchService {

    private final NoticeSearchRepository noticeSearchRepository;
    private final DepartmentAliasService departmentAliasService;

    @Override
    public Page<NoticeResponseDTO> search(SearchRequestDTO req) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, req.getPage()),
                Math.max(1, req.getSize())
        );

        var resolution = departmentAliasService.resolve(req.getDepartments());
        var categories = resolution.categories().isEmpty()
                ? req.effectiveCategories()
                : resolution.categories();
        var aliases = resolution.aliases();

        return noticeSearchRepository
                .search(
                        req.normalizedKeyword(),
                        categories,
                        aliases,
                        aliases.size(),
                        pageable
                )
                .map(NoticeResponseDTO::from);
    }
}
