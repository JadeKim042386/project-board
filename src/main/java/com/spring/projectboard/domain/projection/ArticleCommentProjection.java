package com.spring.projectboard.domain.projection;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.domain.UserAccount;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;
import java.util.Set;

@Projection(name = "withUserAccount", types = ArticleComment.class)
public interface ArticleCommentProjection {
    Long getId();
    String getContent();
    UserAccount getUserAccount();
    Long getParentCommentId();
    LocalDateTime getCreatedAt();
    String getCreatedBy();
    LocalDateTime getModifiedAt();
    String getModifiedBy();
}
