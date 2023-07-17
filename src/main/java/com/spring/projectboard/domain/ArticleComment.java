package com.spring.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.annotation.Order;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList = "content"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy")
})
@Entity
public class ArticleComment extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(optional = false)
    Article article; //게시글 (ID)

    @Setter
    @Column(nullable = false, length = 500)
    private String content; //본문

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")
    UserAccount userAccount;

    @Setter
    @Column(updatable = false)
    private Long parentCommentId;

    @ToString.Exclude
    @OrderBy("createdAt ASC")
    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL)
    private Set<ArticleComment> childComments = new LinkedHashSet<>();

    protected ArticleComment() {
    }

    private ArticleComment(UserAccount userAccount, Article article, String content, Long parentCommentId) {
        this.userAccount = userAccount;
        this.article = article;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public static ArticleComment of(UserAccount userAccount, Article article, String content) {
        return  new ArticleComment(userAccount, article, content, null);
    }

    public void addChildComment(ArticleComment child) {
        child.setParentCommentId(this.getId());
        this.getChildComments().add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleComment that)) return false;
        return this.getId() != null && Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
