package uos.aloc.scholar.search.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.repository.NoticeSearchRepository;
import uos.aloc.scholar.search.service.DepartmentAliasService;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeSearchRepository noticeSearchRepository;

    @Autowired
    private DepartmentAliasService departmentAliasService;

    private int postNumberSequence = 1;

    @BeforeEach
    void setUp() {
        noticeSearchRepository.deleteAll();
        postNumberSequence = 1;

        noticeSearchRepository.saveAll(List.of(
                notice(NoticeCategory.COLLEGE_ENGINEERING, "컴퓨터과학부 공지", "컴퓨터과학부", LocalDate.now()),
                notice(NoticeCategory.COLLEGE_ENGINEERING, "공과대 소식", "공과대학", LocalDate.now().minusDays(1)),
                notice(NoticeCategory.COLLEGE_ENGINEERING, "기계정보 공지", "기계정보공학과", LocalDate.now().minusDays(2)),
                notice(NoticeCategory.COLLEGE_SOCIAL_SCIENCES, "행정학과 공지", "행정학과", LocalDate.now())
        ));
    }

    @Test
    void searchByDepartmentReturnsOnlyEngineeringWithAllowedDepartments() throws Exception {
        String response = mockMvc.perform(get("/notices/search")
                        .param("department", "컴퓨터과학부")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode content = root.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);

        Set<String> allowed = new LinkedHashSet<>(
                departmentAliasService.resolve(List.of("컴퓨터과학부")).aliases()
        );

        for (JsonNode notice : content) {
            assertThat(notice.get("category").asText()).isEqualTo("COLLEGE_ENGINEERING");
            assertThat(allowed).contains(notice.get("department").asText());
        }
    }

    private Notice notice(NoticeCategory category, String title, String department, LocalDate postedDate) {
        Notice notice = new Notice();
        notice.setCategory(category);
        notice.setTitle(title);
        notice.setSummary("요약");
        notice.setLink("https://example.com/" + UUID.randomUUID());
        notice.setPostedDate(postedDate);
        notice.setDepartment(department);
        notice.setPostNumber(postNumberSequence++);
        notice.setViewCount(0);
        return notice;
    }
}
