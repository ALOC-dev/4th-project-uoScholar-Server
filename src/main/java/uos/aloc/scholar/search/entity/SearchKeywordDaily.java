package uos.aloc.scholar.search.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "search_keyword_daily",
       uniqueConstraints = @UniqueConstraint(name = "unique_keyword_day", columnNames = {"keyword", "day"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchKeywordDaily {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "count", nullable = false)
    private int count;

    public void inc() { this.count += 1; }
}
