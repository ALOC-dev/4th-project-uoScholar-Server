package uos.aloc.scholar.search.service;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DepartmentFilterRegistry {

    private final Map<String, List<String>> aliasMap = new ConcurrentHashMap<>();

    public void register(String key, List<String> aliases) {
        if (key == null) {
            return;
        }
        String normalizedKey = normalize(key);
        if (normalizedKey.isEmpty()) {
            return;
        }
        aliasMap.put(normalizedKey, normalizeAliases(aliases));
    }

    public List<String> resolveAliases(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> !s.isEmpty())
                .flatMap(key -> aliasMap.getOrDefault(key, List.of(key)).stream())
                .map(alias -> alias.toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> normalizeAliases(Collection<String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return Collections.emptyList();
        }
        return aliases.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
