package uos.aloc.scholar.search.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.filter.DepartmentFilterRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class SearchRequestDTO {
    private String keyword = "";
    private List<NoticeCategory> category = new ArrayList<>();
    private int page = 0;
    private int size = 15;

    // 추가: 지정한 카테고리만 사용할지 여부 (기본 false: ACADEMIC/GENERAL 자동 포함)
    private boolean exact = false;

    @Setter(AccessLevel.NONE)
    private List<String> departments = new ArrayList<>();

    public void setDepartments(List<String> departments) {
        this.departments = sanitizeDepartments(departments);
    }

    public void setDepartment(List<String> departments) {
        setDepartments(departments);
    }

    public void setDepartment(String departmentsCsv) {
        if (departmentsCsv == null) {
            this.departments = new ArrayList<>();
            return;
        }
        List<String> parsed = Arrays.stream(departmentsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
        setDepartments(parsed);
    }

    public List<NoticeCategory> effectiveCategories() {
        return computeEffectiveCategories(null);
    }

    public List<NoticeCategory> effectiveCategories(DepartmentFilterRegistry registry) {
        if (registry == null) {
            return effectiveCategories();
        }

        List<String> sanitizedDepartments = currentDepartments();
        if (sanitizedDepartments.isEmpty()) {
            return effectiveCategories();
        }

        Set<NoticeCategory> fromRegistry = new LinkedHashSet<>();
        for (String dept : sanitizedDepartments) {
            Optional<DepartmentFilterRegistry.DepartmentFilter> filter = registry.find(dept);
            filter.ifPresent(f -> fromRegistry.addAll(f.categories()));
        }

        if (fromRegistry.isEmpty()) {
            return effectiveCategories();
        }

        return computeEffectiveCategories(fromRegistry);
    }

    public String normalizedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }

    public List<String> resolvedDeptAliases(DepartmentFilterRegistry registry) {
        List<String> sanitizedDepartments = currentDepartments();
        if (sanitizedDepartments.isEmpty() || registry == null) {
            return sanitizedDepartments;
        }

        List<String> resolved = new ArrayList<>();
        for (String dept : sanitizedDepartments) {
            Optional<DepartmentFilterRegistry.DepartmentFilter> filter = registry.find(dept);
            resolved.add(filter.map(DepartmentFilterRegistry.DepartmentFilter::canonicalAlias).orElse(dept));
        }
        return resolved;
    }

    private List<NoticeCategory> computeEffectiveCategories(Set<NoticeCategory> base) {
        if (exact) {
            if (base != null && !base.isEmpty()) {
                return new ArrayList<>(base);
            }
            if (category == null || category.isEmpty()) {
                return List.of(NoticeCategory.ACADEMIC);
            }
            return new ArrayList<>(new LinkedHashSet<>(category));
        }

        Set<NoticeCategory> set = new LinkedHashSet<>();
        set.add(NoticeCategory.ACADEMIC);
        set.add(NoticeCategory.GENERAL);
        if (base != null) {
            set.addAll(base);
        }
        if (category != null) {
            set.addAll(category);
        }
        return new ArrayList<>(set);
    }

    private static List<String> sanitizeDepartments(List<String> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String value : source) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                unique.add(trimmed);
            }
        }
        return new ArrayList<>(unique);
    }

    private List<String> currentDepartments() {
        if (departments == null || departments.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(departments);
    }
}
