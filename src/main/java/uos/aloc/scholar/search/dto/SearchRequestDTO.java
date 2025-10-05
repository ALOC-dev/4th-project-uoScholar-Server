package uos.aloc.scholar.search.dto;

import uos.aloc.scholar.crawler.entity.*;
import uos.aloc.scholar.search.service.DepartmentFilterRegistry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SearchRequestDTO {
    private String keyword = "";
    private List<NoticeCategory> category = new ArrayList<>();
    private List<String> department = new ArrayList<>();
    private int page = 0;
    private int size = 15;

    // 추가: 지정한 카테고리만 사용할지 여부 (기본 false: ACADEMIC/GENERAL 자동 포함)
    private boolean exact = false;

    public List<NoticeCategory> effectiveCategories() {
        if (exact) {
            // exact=true면 사용자가 준 카테고리만 사용 (없으면 학사만 기본값으로)
            if (category == null || category.isEmpty()) {
                return List.of(NoticeCategory.ACADEMIC);
            }
            return new ArrayList<>(new LinkedHashSet<>(category));
        }
        // 기본 동작: 선택 + ACADEMIC + GENERAL 합집합
        Set<NoticeCategory> set = new LinkedHashSet<>();
        set.add(NoticeCategory.ACADEMIC);
        set.add(NoticeCategory.GENERAL);
        if (category != null) set.addAll(category);
        return new ArrayList<>(set);
    }

    public String normalizedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }

    public List<String> departmentAliases(DepartmentFilterRegistry registry) {
        if (registry == null) {
            return List.of();
        }
        return registry.resolveAliases(department);
    }
}
