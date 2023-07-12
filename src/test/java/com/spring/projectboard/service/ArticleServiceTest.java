package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.repository.ArticleRepository;
import com.spring.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("검색어로 게시글 페이지 반환")
    @Test
    void searchArticles() {
        // Given
        String keyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(keyword, pageable)).willReturn(Page.empty());
        // When
        Page<ArticleDto> articles = sut.searchArticles(SearchType.TITLE, keyword, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(keyword, pageable);
    }

    @DisplayName("검색어 없이 게시글 페이지 반환")
    @Test
    void noSearchArticles() {
        // Given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());
        // When
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("해시태그 검색하여 게시글 페이지 반환")
    @Test
    void searchArticlesWithHashtag() {
        // Given
        String hashtag = "#java";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));
        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtag, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtag(hashtag, pageable);
    }

    @DisplayName("검색어 없이 해시태그 검색하여 게시글 페이지 반환")
    @Test
    void noSearchArticlesWithHashtag() {
        // Given
        Pageable pageable = Pageable.ofSize(20);

        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("해시태그 리스트 조회")
    @Test
    void getHashtags(){
        // Given
        List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
        given(articleRepository.findAllDistinctHashtags()).willReturn(expectedHashtags);
        // When
        List<String> actualHashtags = sut.getHashtags();
        // Then
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(articleRepository).should().findAllDistinctHashtags();
    }

    @DisplayName("ID로 댓글 달린 게시글 조회")
    @Test
    void findArticleWithComments() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        // When
        ArticleWithCommentsDto dto = sut.getArticleWithComments(articleId);
        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("[예외] ID로 존재하지 않는 댓글 달린 게시글 조회")
    @Test
    void findNotExistArticleWithComments() {
        // Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());
        // When

        // Then
        assertThatThrownBy(() -> sut.getArticleWithComments(articleId)).isInstanceOf(EntityNotFoundException.class);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("ID로 게시글 조회")
    @Test
    void findArticle() {
        // Given
        Long article_id = 1L;
        Article article = createArticle();
        given(articleRepository.findById(article_id)).willReturn(Optional.of(article));
        // When
        ArticleDto dto = sut.getArticle(article_id);
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

    @DisplayName("게시글 저장")
    @Test
    void saveArticle() {
        // Given
        ArticleDto dto = createArticleDto();
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());
        // When
        sut.saveArticle(dto);
        // Then
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글 수정")
    @Test
    void updateArticle() {
        // Given
        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content", "new hashtag");
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());
        // When
        sut.updateArticle(articleDto.id(), articleDto);
        // Then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .hasFieldOrPropertyWithValue("hashtag", articleDto.hashtag());
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
    }

    @DisplayName("[예외] 없는 게시글 수정")
    @Test
    void updateNotExistArticle() {
        // Given
        ArticleDto articleDto = createArticleDto("new title", "new content", "new hashtag");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateArticle(articleDto.id(), articleDto);
        // Then
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("게시글 삭제")
    @Test
    void deleteArticle() {
        // Given
        Long article_id = 1L;
        String userId = "joo";
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(article_id, userId);
        // When
        sut.deleteArticle(article_id, userId);
        // Then
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(article_id, userId);
    }

    @DisplayName("게시글 수 반환")
    @Test
    void countArticles() {
        // Given
        long expected = 0L;
        given(articleRepository.count()).willReturn(expected);
        // When
        long actual = sut.getArticleCount();
        // Then
        assertThat(actual).isEqualTo(expected);
        then(articleRepository).should().count();
    }

    private Article createArticle() {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content",
                "hashtag"
        );
        ReflectionTestUtils.setField(article, "id", 1L);
        return article;
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
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "joo",
                "pw",
                "joo@gmail.com",
                "joo",
                "memo",
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }
}
