package uos.aloc.scholar.search.dto;

import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchRequestDTO {

    private String keyword = "";
    private List<NoticeCategory> category = new ArrayList<>();
    private int page = 0;
    private int size = 15;
    private boolean exact = false;
    private List<String> departments = new ArrayList<>();

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword;
    }

    public List<NoticeCategory> getCategory() {
        return category == null ? List.of() : Collections.unmodifiableList(category);
    }

    public void setCategory(List<NoticeCategory> category) {
        if (category == null) {
            this.category = new ArrayList<>();
        } else {
            this.category = new ArrayList<>(category);
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isExact() {
        return exact;
    }

    public void setExact(boolean exact) {
        this.exact = exact;
    }

    public List<String> getDepartments() {
        if (departments == null) {
            return List.of();
        }
        return Collections.unmodifiableList(departments);
    }

    public void setDepartments(List<String> departments) {
        this.departments = normalizeDepartments(departments);
    }

    /**
     * `department` 단일 파라미터(alias)를 `departments` 리스트로 매핑하기 위한 Setter.
     */
    public void setDepartment(List<String> departments) {
        setDepartments(departments);
    }

    public void setDepartment(String department) {
        if (department == null) {
            this.departments = new ArrayList<>();
        } else {
            this.departments = normalizeDepartments(List.of(department));
        }
    }

    public List<NoticeCategory> effectiveCategories() {
        if (exact) {
            if (category == null || category.isEmpty()) {
                return List.of(NoticeCategory.ACADEMIC);
            }
            return new ArrayList<>(new LinkedHashSet<>(category));
        }

        Set<NoticeCategory> set = new LinkedHashSet<>();
        set.add(NoticeCategory.ACADEMIC);
        set.add(NoticeCategory.GENERAL);
        if (category != null) {
            set.addAll(category);
        }
        return new ArrayList<>(set);
    }

    public String normalizedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }

    private static List<String> normalizeDepartments(List<String> departments) {
        if (departments == null) {
            return new ArrayList<>();
        }
        return departments.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
