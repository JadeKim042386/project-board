package com.spring.projectboard.repository.querydsl;

import com.spring.projectboard.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ArticleRepositoryCustom {
    Page<Article> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable);
}
