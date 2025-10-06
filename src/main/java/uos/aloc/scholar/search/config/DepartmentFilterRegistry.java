package uos.aloc.scholar.search.config;

import org.springframework.stereotype.Component;
import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DepartmentFilterRegistry {

    public record DepartmentMeta(NoticeCategory category, List<String> aliases) {}

    private final Map<String, DepartmentMeta> table = new HashMap<>();

    public DepartmentFilterRegistry() {
        // ───────────────────── 정경대학 ─────────────────────
        put("행정학과", NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("행정학과", "정경대학"));
        put("국제관계학과", NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("국제관계학과", "정경대학"));
        put("경제학부", NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("경제학부", "정경대학"));
        put("사회복지학과", NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("사회복지학과", "정경대학"));
        put("세무학과", NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("세무학과", "정경대학"));

        // ───────────────────── 경영대학 ─────────────────────
        put("경영학부", NoticeCategory.COLLEGE_BUSINESS, List.of("경영학부", "경영대학"));

        // ───────────────────── 공과대학 ─────────────────────
        put("전자전기컴퓨터공학부", NoticeCategory.COLLEGE_ENGINEERING, List.of("전자전기컴퓨터공학부", "공과대학"));
        put("화학공학과", NoticeCategory.COLLEGE_ENGINEERING, List.of("화학공학과", "공학교육혁신센터", "공과대학"));
        put("기계정보공학과", NoticeCategory.COLLEGE_ENGINEERING, List.of("기계정보공학과", "공학교육혁신센터", "공과대학"));
        put("신소재공학과", NoticeCategory.COLLEGE_ENGINEERING, List.of("신소재공학과", "공학교육혁신센터", "공과대학"));
        put("토목공학과", NoticeCategory.COLLEGE_ENGINEERING, List.of("토목공학과", "공학교육혁신센터", "공과대학"));
        put("컴퓨터과학부", NoticeCategory.COLLEGE_ENGINEERING, List.of("컴퓨터과학부", "공학교육혁신센터", "공과대학"));
        put("인공지능학과", NoticeCategory.COLLEGE_ENGINEERING, List.of("인공지능학과", "공학교육혁신센터", "공과대학"));

        // ───────────────────── 인문대학 ─────────────────────
        put("영어영문학과", NoticeCategory.COLLEGE_HUMANITIES, List.of("영어영문학과", "교육대학원", "인문대학"));
        put("국어국문학과", NoticeCategory.COLLEGE_HUMANITIES, List.of("국어국문학과", "교육대학원", "인문대학"));
        put("국사학과", NoticeCategory.COLLEGE_HUMANITIES, List.of("국사학과", "교육대학원", "인문대학"));
        put("철학과", NoticeCategory.COLLEGE_HUMANITIES, List.of("철학과", "교육대학원", "인문대학"));
        put("중국어문화학과", NoticeCategory.COLLEGE_HUMANITIES, List.of("중국어문화학과", "교육대학원", "인문대학"));

        // ───────────────────── 자연과학대학 ─────────────────────
        put("수학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("수학과", "자연과학대학"));
        put("통계학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("통계학과", "자연과학대학"));
        put("물리학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("물리학과", "자연과학대학"));
        put("생명과학학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("생명과학학과", "자연과학대학"));
        put("환경원예학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("환경원예학과", "자연과학대학"));
        put("융합응용화학과", NoticeCategory.COLLEGE_NATURAL_SCIENCES, List.of("융합응용화학과", "자연과학대학"));

        // ───────────────────── 도시과학대학 ─────────────────────
        put("건축학부(건축공학)", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("건축학부", "도시과학대학", "공학교육혁신센터", "공과대학"));
        put("건축학부(건축학)", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("건축학부", "도시과학대학", "건축학"));
        put("도시공학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("도시공학과", "도시과학대학"));
        put("교통공학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("교통공학과", "도시과학대학"));
        put("조경학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("조경학과", "도시과학대학"));
        put("도시행정학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("도시행정학과", "도시과학대학"));
        put("도시사회학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("도시사회학과", "도시과학대학"));
        put("공간정보공학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("공간정보공학과", "도시과학대학"));
        put("소방방재학과", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("소방방재학과", "도시과학대학"));
        put("환경공학부", NoticeCategory.COLLEGE_URBAN_SCIENCE, List.of("환경공학부", "도시과학대학", "공학교육혁신센터", "공과대학"));

        // ───────────────────── 예술체육대학 ─────────────────────
        put("음악학과", NoticeCategory.COLLEGE_ARTS_SPORTS, List.of("음악학과", "예술체육대학"));
        put("디자인학과", NoticeCategory.COLLEGE_ARTS_SPORTS, List.of("디자인학과", "예술체육대학"));
        put("조각학과", NoticeCategory.COLLEGE_ARTS_SPORTS, List.of("조각학과", "환경조각학과", "예술체육대학"));
        put("스포츠과학과", NoticeCategory.COLLEGE_ARTS_SPORTS, List.of("스포츠과학과", "예술체육대학"));

        // ───────────────────── 자유융합대학 ─────────────────────
        put("자유전공학부", NoticeCategory.COLLEGE_LIBERAL_CONVERGENCE, List.of("자유전공학부", "자유융합대학"));
        put("융합전공학부", NoticeCategory.COLLEGE_LIBERAL_CONVERGENCE, List.of("융합전공학부", "자유융합대학"));
        put("첨단융합학부", NoticeCategory.COLLEGE_LIBERAL_CONVERGENCE, List.of("첨단융합학부", "자유융합대학"));
    }

    private void put(String dept, NoticeCategory category, List<String> aliases) {
        table.put(dept, new DepartmentMeta(category, aliases));
    }

    public boolean contains(String dept) {
        return table.containsKey(dept);
    }

    public DepartmentMeta getMeta(String dept) {
        DepartmentMeta meta = table.get(dept);
        if (meta == null) throw new IllegalArgumentException("Unknown department: " + dept);
        return meta;
    }
}
