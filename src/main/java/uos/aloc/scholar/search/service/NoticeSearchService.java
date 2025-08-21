package uos.aloc.scholar.search.service;

import uos.aloc.scholar.search.dto.NoticeResponseDTO;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import org.springframework.data.domain.Page;

public interface NoticeSearchService {
    Page<NoticeResponseDTO> search(SearchRequestDTO req);
}
