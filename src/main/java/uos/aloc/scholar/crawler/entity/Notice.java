package uos.aloc.scholar.crawler.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글번호는 유일해야 하므로 unique 제약조건 추가
    @Column(name = "post_number", unique = true)
    private Integer postNumber;

    private String title;
    private String link;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String vector;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    // 작성부서
    private String department;

    // 기본 생성자
    public Notice() {
    }

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
  
    public Integer getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(Integer postNumber) {
        this.postNumber = postNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }
}
