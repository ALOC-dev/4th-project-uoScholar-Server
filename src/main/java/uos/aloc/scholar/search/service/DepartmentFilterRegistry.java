package uos.aloc.scholar.search.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry utility that will later provide department alias resolution and
 * category mappings for department based searches.  The current implementation
 * focuses on sanitising alias input while keeping the API surface ready for
 * future enhancements.
 */
@Component
public class DepartmentFilterRegistry {

    /**
     * Normalises the provided department aliases.  Null or blank aliases are
     * ignored and the remaining entries are trimmed.  The returned list is a
     * mutable copy so callers may safely modify it.
     *
     * @param rawAliases aliases supplied by the request (may be {@code null})
     * @return distinct, trimmed aliases (never {@code null})
     */
    public List<String> resolveAliases(List<String> rawAliases) {
        if (rawAliases == null || rawAliases.isEmpty()) {
            return Collections.emptyList();
        }

        return rawAliases.stream()
                .filter(alias -> alias != null && !alias.isBlank())
                .map(String::trim)
                .distinct()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
