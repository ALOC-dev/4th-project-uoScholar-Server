package uos.aloc.scholar.search.dto;

import uos.aloc.scholar.crawler.entity.*;
import uos.aloc.scholar.search.config.DepartmentFilterRegistry;

import lombok.Getter;
import lombok.Setter;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import java.util.*;

@Getter
@Setter
public class SearchRequestDTO {
    private String keyword = "";
    private NoticeCategory category;
    private List<String> departments = new ArrayList<>();
    private int page = 0;
    private int size = 15;

    /**
     * 단일 쿼리 파라미터(department=컴퓨터과학부,행정학과) CSV 파싱용
     * - 공백/빈 토큰은 무시
     * - 중복 제거
     */
    public void setDepartment(String csv) {
        if (!StringUtils.hasText(csv)) return;
        List<String> parsed = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (!parsed.isEmpty()) {
            // 기존 리스트에 추가(중복 방지)
            Set<String> set = new LinkedHashSet<>(departments);
            set.addAll(parsed);
            this.departments = new ArrayList<>(set);
        }
    }

    /** 키워드 정규화(트림 후 빈 문자열이면 null) */
    public String normalizedKeyword() {
        if (!StringUtils.hasText(keyword)) return null;
        String k = keyword.trim();
        return k.isEmpty() ? null : k;
    }

    /**
     * 유효성 검증(서비스 진입 시 호출 권장)
     * - departments 와 category 동시 전송 금지
     * - 둘 다 미존재 금지
     * - departments 내 알 수 없는 학과명 금지
     */
    public void validateForSearch(DepartmentFilterRegistry registry) {
        boolean hasDept = departments != null && !departments.isEmpty();
        boolean hasCategory = category != null;

        if (hasDept && hasCategory) {
            throw new IllegalArgumentException("잘못된 요청: department와 category를 함께 보낼 수 없습니다.");
        }
        if (!hasDept && !hasCategory) {
            throw new IllegalArgumentException("잘못된 요청: department 또는 category 중 하나는 반드시 포함되어야 합니다.");
        }
        if (hasDept) {
            List<String> unknown = departments.stream()
                    .filter(d -> !registry.contains(d))
                    .collect(Collectors.toList());
            if (!unknown.isEmpty()) {
                throw new IllegalArgumentException("알 수 없는 학과 식별자: " + String.join(", ", unknown));
            }
        }
    }

    /**
     * 효과적 카테고리 계산
     * - 학과 공지 모드: departments → (학과별 단과대)들의 합집합
     * - 카테고리 공지 모드: 단일 category
     */
    public List<NoticeCategory> effectiveCategories(DepartmentFilterRegistry registry) {
        boolean hasDept = departments != null && !departments.isEmpty();
        if (hasDept) {
            return departments.stream()
                    .map(registry::getMeta)          // validateForSearch 로 unknown 차단됨
                    .map(DepartmentFilterRegistry.DepartmentMeta::category)
                    .distinct()
                    .collect(Collectors.toList());
        }
        // 카테고리 공지 모드
        return List.of(category);
    }

    /**
     * 학과 공지 모드에서 사용할 작성부서 alias 해석
     * - 학과 공지 모드: 각 학과의 alias 전부 합집합(distinct)
     * - 카테고리 공지 모드: 부서 필터 미적용 → 빈 리스트 반환(deptSize=0)
     */
    public List<String> resolvedDeptAliases(DepartmentFilterRegistry registry) {
        boolean hasDept = departments != null && !departments.isEmpty();
        if (!hasDept) return List.of();

        return departments.stream()
                .map(registry::getMeta)
                .map(DepartmentFilterRegistry.DepartmentMeta::aliases)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }



}
