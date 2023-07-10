package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<Article> searchArticles(SearchType searchType, String title, Pageable pageable) {
        return List.of();
    }

    @Transactional(readOnly = true)
    public ArticleWithCommentsDto getArticle(Long articleId) {
        return null;
    }

    public void saveArticle(ArticleDto dto) {
    }

    public void updateArticle(ArticleDto articleDto) {
    }

    public void deleteArticle(Long articleId) {
    }
}
