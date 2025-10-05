package uos.aloc.scholar.search.service;

import org.springframework.stereotype.Component;
import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.*;

@Component
public class DepartmentAliasService {

    private final Map<String, DepartmentGroup> directory;

    public DepartmentAliasService() {
        Map<String, DepartmentGroup> map = new HashMap<>();
        register(map, NoticeCategory.COLLEGE_SOCIAL_SCIENCES, List.of("행정학과", "정경대학"));
        register(map, NoticeCategory.COLLEGE_ENGINEERING, List.of("컴퓨터과학부", "컴퓨터과학과", "공과대학"));
        this.directory = Collections.unmodifiableMap(map);
    }

    private void register(Map<String, DepartmentGroup> map, NoticeCategory category, List<String> aliases) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String alias : aliases) {
            String key = normalize(alias);
            if (!key.isEmpty()) {
                normalized.add(key);
            }
        }
        if (normalized.isEmpty()) {
            return;
        }
        DepartmentGroup group = new DepartmentGroup(category, List.copyOf(normalized));
        for (String alias : normalized) {
            map.put(alias, group);
        }
    }

    public DepartmentResolution resolve(List<String> departments) {
        if (departments == null || departments.isEmpty()) {
            return DepartmentResolution.empty();
        }

        LinkedHashSet<NoticeCategory> categories = new LinkedHashSet<>();
        LinkedHashSet<String> aliases = new LinkedHashSet<>();

        for (String raw : departments) {
            String key = normalize(raw);
            if (key.isEmpty()) continue;

            DepartmentGroup group = directory.get(key);
            if (group == null) continue;

            categories.add(group.category());
            aliases.addAll(group.aliases());
        }

        if (categories.isEmpty()) {
            return DepartmentResolution.empty();
        }

        return new DepartmentResolution(List.copyOf(categories), List.copyOf(aliases));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record DepartmentGroup(NoticeCategory category, List<String> aliases) { }

    public record DepartmentResolution(List<NoticeCategory> categories, List<String> aliases) {
        private static DepartmentResolution empty() {
            return new DepartmentResolution(List.of(), List.of());
        }
    }
}
