package uos.aloc.scholar.search.dto;

import lombok.Getter;
import lombok.Setter;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.service.DepartmentFilterRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SearchRequestDTO {
    private String keyword = "";
    private List<NoticeCategory> category = new ArrayList<>();
    private List<String> departments = new ArrayList<>();
    private int page = 0;
    private int size = 15;

    // 추가: 지정한 카테고리만 사용할지 여부 (기본 false: ACADEMIC/GENERAL 자동 포함)
    private boolean exact = false;

    public List<NoticeCategory> effectiveCategories() {
        return effectiveCategories(null);
    }

    public List<NoticeCategory> effectiveCategories(DepartmentFilterRegistry deptRegistry) {
        return computeEffectiveCategories();
    }

    private List<NoticeCategory> computeEffectiveCategories() {
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

    public List<String> resolvedDeptAliases(DepartmentFilterRegistry deptRegistry) {
        List<String> raw = departments == null ? List.of() : departments;
        if (raw.isEmpty()) {
            return List.of();
        }

        List<String> resolved = deptRegistry == null ? raw : deptRegistry.resolveAliases(raw);
        if (resolved == null || resolved.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(resolved));
    }

    public String normalizedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }
}
