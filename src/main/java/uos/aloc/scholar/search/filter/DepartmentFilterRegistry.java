package uos.aloc.scholar.search.filter;

import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentFilterRegistry {

    Optional<DepartmentFilter> find(String department);

    default List<String> resolveAliases(List<String> departments) {
        if (departments == null || departments.isEmpty()) {
            return List.of();
        }

        List<String> resolved = new ArrayList<>(departments.size());
        for (String department : departments) {
            if (department == null) {
                continue;
            }

            Optional<DepartmentFilter> filter = find(department);
            resolved.add(filter.map(DepartmentFilter::canonicalAlias).orElse(department));
        }

        return resolved;
    }

    interface DepartmentFilter {
        String canonicalAlias();

        Collection<NoticeCategory> categories();
    }
}
