package uos.aloc.scholar.search.service;

import uos.aloc.scholar.search.config.DepartmentFilterRegistry;
import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import uos.aloc.scholar.crawler.entity.Notice;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;


@Service
@RequiredArgsConstructor
public class NoticeSearchServiceImpl implements NoticeSearchService {

    private final NoticeSearchRepository repository;
    private final DepartmentFilterRegistry deptRegistry;

    @Override
    public Page<NoticeResponseDTO> search(SearchRequestDTO req, Pageable pageable) {
        try {
            // 요청 규칙 검증 (상호배타 + 필수 + 학과명 검증)
            req.validateForSearch(deptRegistry);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        var categories  = req.effectiveCategories(deptRegistry);  // 학과모드면 합집합, 아니면 단일 category
        var deptAliases = req.resolvedDeptAliases(deptRegistry);  // 학과모드면 alias들, 카테고리모드면 빈 리스트
        var kw          = req.normalizedKeyword();

        // 기본 정렬: postedDate DESC, id DESC
        Sort sort = Sort.by(Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
        Pageable pageReq = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Notice> entityPage = repository.search(categories, deptAliases, kw, pageReq);

        // ✅ 여기서 엔티티 → DTO 명시적 매핑
        return entityPage.map(NoticeResponseDTO::fromEntity);
    }
}
