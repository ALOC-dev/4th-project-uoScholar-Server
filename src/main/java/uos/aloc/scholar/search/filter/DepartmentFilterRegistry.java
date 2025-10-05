package uos.aloc.scholar.search.filter;

import uos.aloc.scholar.crawler.entity.NoticeCategory;

import java.util.Collection;
import java.util.Optional;

public interface DepartmentFilterRegistry {

    Optional<DepartmentFilter> find(String department);

    interface DepartmentFilter {
        String canonicalAlias();

        Collection<NoticeCategory> categories();
    }
}
