package uos.aloc.scholar.crawler.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "notice",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_notice_cat_postnum", columnNames = {"category", "post_number"})
    },
    indexes = {
        @Index(name = "idx_notice_cat_date", columnList = "category, posted_date, id")
    }
)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 카테고리(일반/학사/단과대)인지 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeCategory category;

    /** 보드 내 게시글 번호 */
    @Column(name = "post_number")
    private Integer postNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String link;

    /** LLM 요약 결과 (리스트/검색용) */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    /** 임베딩 벡터(JSON 문자열 등으로 보관) */
    @Lob
    @Basic(fetch = FetchType.LAZY) // 목록 조회 시 불러오지 않도록
    @Column(name = "embedding_vector", columnDefinition = "LONGTEXT")
    private String embeddingVector;

    @Column(name = "posted_date", nullable = false)
    private LocalDate postedDate;

    /** 작성부서(원문 표기 그대로 저장) */
    private String department;

    @Column(name = "view_count", nullable = true) // nullable=true는 DDL 힌트(스키마 생성 시)
    private Integer viewCount = 0;                // 엔티티 기본값 0 (INSERT 시 null 방지)

    public Notice() {}

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NoticeCategory getCategory() { return category; }
    public void setCategory(NoticeCategory category) { this.category = category; }

    public Integer getPostNumber() { return postNumber; }
    public void setPostNumber(Integer postNumber) { this.postNumber = postNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEmbeddingVector() { return embeddingVector; }
    public void setEmbeddingVector(String embeddingVector) { this.embeddingVector = embeddingVector; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

}


