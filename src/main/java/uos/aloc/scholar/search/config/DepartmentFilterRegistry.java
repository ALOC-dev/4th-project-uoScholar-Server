package uos.aloc.scholar.search.config;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import uos.aloc.scholar.crawler.entity.NoticeCategory;
import uos.aloc.scholar.search.filter.DepartmentFilterRegistry.DepartmentFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class DepartmentFilterRegistry implements uos.aloc.scholar.search.filter.DepartmentFilterRegistry {

    private static final String YAML_DATA = """
국어국문학과:
  category: COLLEGE_HUMANITIES
  aliases:
    - 국문학과
    - 국문과
영어영문학과:
  category: COLLEGE_HUMANITIES
  aliases:
    - 영어영문과
    - 영문과
    - 영어과
중국어문화학과:
  category: COLLEGE_HUMANITIES
  aliases:
    - 중국어과
    - 중문과
국사학과:
  category: COLLEGE_HUMANITIES
  aliases:
    - 역사학과
    - 한국사학과
철학과:
  category: COLLEGE_HUMANITIES
  aliases:
    - 철학전공
    - 철학학과
경제학부:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 경제학과
    - 경제과
    - 경제전공
사회복지학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 사회복지과
    - 복지학과
사회학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 사회과
    - 사회전공
국제관계학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 국제관계과
    - 국제정치학과
정치외교학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 정치외교과
    - 정치학과
    - 외교학과
행정학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 행정과
    - 공공행정학과
도시사회학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 도시사회과
도시행정학과:
  category: COLLEGE_SOCIAL_SCIENCES
  aliases:
    - 도시행정과
경영학부:
  category: COLLEGE_BUSINESS
  aliases:
    - 경영학과
    - 경영과
    - 경영전공
세무학과:
  category: COLLEGE_BUSINESS
  aliases:
    - 세무과
    - 세무전공
수학과:
  category: COLLEGE_NATURAL_SCIENCES
  aliases:
    - 수리과학과
통계학과:
  category: COLLEGE_NATURAL_SCIENCES
  aliases:
    - 통계과
    - 통계전공
물리학과:
  category: COLLEGE_NATURAL_SCIENCES
  aliases:
    - 물리과
화학과:
  category: COLLEGE_NATURAL_SCIENCES
  aliases:
    - 화학전공
생명과학과:
  category: COLLEGE_NATURAL_SCIENCES
  aliases:
    - 생명과
    - 생명과학전공
기계정보공학과:
  category: COLLEGE_ENGINEERING
  aliases:
    - 기계정보과
    - 기계공학과
신소재공학과:
  category: COLLEGE_ENGINEERING
  aliases:
    - 신소재과
    - 신소재전공
전자전기컴퓨터공학부:
  category: COLLEGE_ENGINEERING
  aliases:
    - 전자전기공학부
    - 전자공학과
    - 전기공학과
    - 컴퓨터공학과
    - 컴퓨터과학부
화학공학과:
  category: COLLEGE_ENGINEERING
  aliases:
    - 화공과
    - 화공생명공학과
건설시스템공학과:
  category: COLLEGE_ENGINEERING
  aliases:
    - 건설시스템과
    - 건설공학과
건축학부(건축학):
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 건축학부
    - 건축학과
건축학부(건축공학):
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 건축공학과
토목공학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 토목과
도시공학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 도시계획과
    - 도시계획학과
교통공학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 교통과
    - 교통전공
환경공학부:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 환경공학과
    - 환경과
공간정보공학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 공간정보과
    - 측량학과
    - 지적학과
조경학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 조경과
부동산학과:
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 부동산과
도시사회학과(도시과학대학):
  category: COLLEGE_URBAN_SCIENCE
  aliases:
    - 도시사회학전공
산업디자인학과:
  category: COLLEGE_ARTS_SPORTS
  aliases:
    - 디자인학부(산업디자인)
    - 산업디자인전공
도시공간디자인학과:
  category: COLLEGE_ARTS_SPORTS
  aliases:
    - 디자인학부(도시공간디자인)
환경조각학과:
  category: COLLEGE_ARTS_SPORTS
  aliases:
    - 환경조각과
음악학과:
  category: COLLEGE_ARTS_SPORTS
  aliases:
    - 음악과
    - 성악과
스포츠과학과:
  category: COLLEGE_ARTS_SPORTS
  aliases:
    - 체육학과
    - 체육과
    - 스포츠과
자유전공학부:
  category: COLLEGE_LIBERAL_CONVERGENCE
  aliases:
    - 자유전공
    - 자율전공학부
데이터사이언스학과:
  category: COLLEGE_LIBERAL_CONVERGENCE
  aliases:
    - 데이터사이언스과
    - 데이터과학과
인공지능학과:
  category: COLLEGE_LIBERAL_CONVERGENCE
  aliases:
    - AI학과
    - 인공지능과
스마트시티융합학과:
  category: COLLEGE_LIBERAL_CONVERGENCE
  aliases:
    - 스마트시티학과
    - 스마트도시학과
""";

    private final Map<String, DepartmentMeta> metaByDepartment;
    private final Map<String, String> aliasIndex;

    public DepartmentFilterRegistry() {
        this.metaByDepartment = Collections.unmodifiableMap(loadYaml());
        this.aliasIndex = Collections.unmodifiableMap(buildAliasIndex(metaByDepartment));
    }

    private Map<String, DepartmentMeta> loadYaml() {
        Yaml yaml = new Yaml();
        Map<String, Object> loaded = yaml.load(YAML_DATA);
        Map<String, DepartmentMeta> map = new LinkedHashMap<>();
        if (loaded == null) {
            return map;
        }
        for (Map.Entry<String, Object> entry : loaded.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Map<?, ?> entryMap)) {
                continue;
            }
            String categoryName = Objects.toString(entryMap.get("category"), null);
            if (categoryName == null) {
                continue;
            }
            NoticeCategory category = NoticeCategory.valueOf(categoryName);
            List<String> aliases = new ArrayList<>();
            Object aliasObj = entryMap.get("aliases");
            if (aliasObj instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item != null) {
                        aliases.add(item.toString());
                    }
                }
            }
            map.put(entry.getKey(), new DepartmentMeta(category, aliases));
        }
        return map;
    }

    private Map<String, String> buildAliasIndex(Map<String, DepartmentMeta> map) {
        Map<String, String> index = new HashMap<>();
        map.forEach((department, meta) -> {
            index.put(department, department);
            for (String alias : meta.aliases()) {
                index.putIfAbsent(alias, department);
            }
        });
        return index;
    }

    public Optional<DepartmentMeta> getMeta(String name) {
        return findCanonicalName(name).map(metaByDepartment::get);
    }

    public boolean contains(String name) {
        return findCanonicalName(name).isPresent();
    }

    public Optional<String> findCanonicalName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        String canonical = aliasIndex.get(normalized);
        if (canonical != null) {
            return Optional.of(canonical);
        }
        return Optional.ofNullable(metaByDepartment.containsKey(normalized) ? normalized : null);
    }

    @Override
    public Optional<DepartmentFilter> find(String department) {
        Optional<String> canonicalName = findCanonicalName(department);
        if (canonicalName.isEmpty()) {
            return Optional.empty();
        }
        DepartmentMeta meta = metaByDepartment.get(canonicalName.get());
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.of(new SimpleDepartmentFilter(canonicalName.get(), meta.category()));
    }

    public Optional<NoticeCategory> lookupCategory(String name) {
        return getMeta(name).map(DepartmentMeta::category);
    }

    public Set<String> departments() {
        return metaByDepartment.keySet();
    }

    public Map<String, DepartmentMeta> asMap() {
        return metaByDepartment;
    }

    public record DepartmentMeta(NoticeCategory category, List<String> aliases) {
        public DepartmentMeta {
            category = Objects.requireNonNull(category, "category");
            aliases = aliases == null ? List.of() : List.copyOf(aliases);
        }
    }

    private static final class SimpleDepartmentFilter implements DepartmentFilter {
        private final String canonicalAlias;
        private final List<NoticeCategory> categories;

        private SimpleDepartmentFilter(String canonicalAlias, NoticeCategory category) {
            this.canonicalAlias = Objects.requireNonNull(canonicalAlias, "canonicalAlias");
            this.categories = List.of(Objects.requireNonNull(category, "category"));
        }

        @Override
        public String canonicalAlias() {
            return canonicalAlias;
        }

        @Override
        public List<NoticeCategory> categories() {
            return categories;
        }
    }
}
