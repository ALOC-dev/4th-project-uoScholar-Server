package uos.aloc.scholar.search.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.dto.SearchRequestDTO;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeSearchServiceImplTest {

    @Mock
    private NoticeSearchRepository noticeSearchRepository;

    private NoticeSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NoticeSearchServiceImpl(noticeSearchRepository, new DepartmentAliasService());
    }

    @Test
    void resolvesDepartmentsToCollegeAndAliases() {
        SearchRequestDTO request = new SearchRequestDTO();
        request.setDepartments(List.of("행정학과"));

        when(noticeSearchRepository.search(anyString(), anyList(), anyList(), anyInt(), any()))
                .thenAnswer(invocation -> Page.empty((Pageable) invocation.getArgument(4)));

        service.search(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NoticeCategory>> categoryCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> aliasCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(noticeSearchRepository).search(
                eq(""),
                categoryCaptor.capture(),
                aliasCaptor.capture(),
                sizeCaptor.capture(),
                any(Pageable.class)
        );

        assertThat(categoryCaptor.getValue())
                .containsExactly(NoticeCategory.COLLEGE_SOCIAL_SCIENCES);
        assertThat(aliasCaptor.getValue())
                .containsExactly("행정학과", "정경대학");
        assertThat(sizeCaptor.getValue()).isEqualTo(2);
    }
}
