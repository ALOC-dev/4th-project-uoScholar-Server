package uos.aloc.scholar.search.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uos.aloc.scholar.crawler.entity.Notice;
import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoticeSearchRepositoryTest {

    @Autowired
    private NoticeSearchRepository noticeSearchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private int postNumberSequence = 1;

    @BeforeEach
    void resetSequence() {
        postNumberSequence = 1;
    }

    @Test
    void searchBypassesDepartmentFilterWhenEmpty() {
        persist(notice(NoticeCategory.COLLEGE_ENGINEERING, "컴퓨터과학부 공지", "컴퓨터과학부", LocalDate.now()));
        persist(notice(NoticeCategory.COLLEGE_ENGINEERING, "기계정보 공지", "기계정보공학과", LocalDate.now().minusDays(1)));

        Page<Notice> page = noticeSearchRepository.search(
                "",
                List.of(NoticeCategory.COLLEGE_ENGINEERING),
                List.of(),
                0,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(Notice::getDepartment)
                .containsExactlyInAnyOrder("컴퓨터과학부", "기계정보공학과");
    }

    @Test
    void searchFiltersByPositiveDepartmentAliases() {
        persist(notice(NoticeCategory.COLLEGE_ENGINEERING, "컴퓨터과학부 공지", "컴퓨터과학부", LocalDate.now()));
        persist(notice(NoticeCategory.COLLEGE_ENGINEERING, "공과대 소식", "공과대학", LocalDate.now().minusDays(1)));
        persist(notice(NoticeCategory.COLLEGE_ENGINEERING, "타과 공지", "기계정보공학과", LocalDate.now().minusDays(2)));

        Page<Notice> page = noticeSearchRepository.search(
                "",
                List.of(NoticeCategory.COLLEGE_ENGINEERING),
                List.of("컴퓨터과학부", "공과대학"),
                2,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(Notice::getDepartment)
                .containsExactly("컴퓨터과학부", "공과대학");
    }

    @Test
    void searchMatchesKeywordAcrossFields() {
        persist(notice(NoticeCategory.COLLEGE_SOCIAL_SCIENCES, "행정학과 공지", "행정학과", LocalDate.now()));
        persist(notice(NoticeCategory.COLLEGE_SOCIAL_SCIENCES, "정경대 소식", "정경대학", LocalDate.now().minusDays(1), "행정 키워드"));
        persist(notice(NoticeCategory.COLLEGE_SOCIAL_SCIENCES, "경제학과 소식", "경제학과", LocalDate.now().minusDays(2)));

        Page<Notice> page = noticeSearchRepository.search(
                "행정",
                List.of(NoticeCategory.COLLEGE_SOCIAL_SCIENCES),
                List.of(),
                0,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(Notice::getTitle)
                .containsExactly("행정학과 공지", "정경대 소식");
    }

    private Notice notice(NoticeCategory category, String title, String department, LocalDate postedDate) {
        return notice(category, title, department, postedDate, "요약");
    }

    private Notice notice(NoticeCategory category, String title, String department, LocalDate postedDate, String summary) {
        Notice notice = new Notice();
        notice.setCategory(category);
        notice.setTitle(title);
        notice.setSummary(summary);
        notice.setLink("https://example.com/" + UUID.randomUUID());
        notice.setPostedDate(postedDate);
        notice.setDepartment(department);
        notice.setPostNumber(postNumberSequence++);
        notice.setViewCount(0);
        return notice;
    }

    private Notice persist(Notice notice) {
        entityManager.persist(notice);
        entityManager.flush();
        entityManager.clear();
        return notice;
    }
}
