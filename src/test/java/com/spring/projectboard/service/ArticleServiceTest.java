package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks private ArticleService sut; //system under test
    @Mock private ArticleRepository articleRepository;

    @DisplayName("검색어로 게시글 페이지 반환")
    @Test
    void searchArticles() {
        // Given
        String keyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitle(keyword, pageable)).willReturn(Page.empty());
        // When
        List<Article> articles = sut.searchArticles(SearchType.TITLE, keyword, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitle(keyword, pageable);
    }

    @DisplayName("검색어 없이 게시글 페이지 반환")
    @Test
    void noSearchArticles() {
        // Given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());
        // When
        List<Article> articles = sut.searchArticles(null, null, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("ID로 게시글 조회")
    @Test
    void findArticle() {
        // Given
        Long article_id = 1L;
        Article article = createArticle();
        given(articleRepository.findById(article_id)).willReturn(Optional.of(article));
        // When
        ArticleWithCommentsDto dto = sut.getArticle(article_id);
        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(article_id);
    }

    @DisplayName("[예외] ID로 없는 게시글 조회")
    @Test
    void findNotExistArticle() {
        // Given
        Long article_id = 0L;
        given(articleRepository.findById(article_id)).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> sut.getArticle(article_id)).isInstanceOf(EntityNotFoundException.class);
        then(articleRepository).should().findById(article_id);
    }

    @DisplayName("게시글 생성")
    @Test
    void saveArticle() {
        // Given
        ArticleDto dto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());
        // When
        sut.saveArticle(dto);
        // Then
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글 수정")
    @Test
    void updateArticle() {
        // Given
        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content", "new hashtag");
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);
        // When
        sut.updateArticle(articleDto);
        // Then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .hasFieldOrPropertyWithValue("hashtag", articleDto.hashtag());
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("없는 게시글 수정")
    @Test
    void updateNotExistArticle() {
        // Given
        ArticleDto articleDto = createArticleDto("new title", "new content", "new hashtag");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateArticle(articleDto);
        // Then
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "hashtag"
        );
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "joo",
                "pw",
                "joo@gmail.com",
                "joo",
                "memo"
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "hashtag");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(
                1L,
                createUserAccount(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }
}
