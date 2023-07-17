package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.Hashtag;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.HashtagDto;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.repository.ArticleRepository;
import com.spring.projectboard.repository.HashtagRepository;
import com.spring.projectboard.repository.UserAccountRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks private ArticleService sut; //system under test
    @Mock private HashtagService hashtagService;
    @Mock private ArticleRepository articleRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private HashtagRepository hashtagRepository;

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
        String hashtagName = "java";
        Article expectedArticle = createArticle();
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtagNames(Set.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(expectedArticle), pageable, 1));
        // When
        Page<ArticleDto> articles = sut.searchArticleDtosViaHashtag(hashtagName, pageable);
        // Then
        assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)), pageable, 1));
        then(articleRepository).should().findByHashtagNames(Set.of(hashtagName), pageable);
    }

    @DisplayName("[예외] 해시태그 검색에 해시태그가 없으면 빈 페이지 반환")
    @Test
    void noSearchArticlesWithNullHashtag() {
        // Given
        Pageable pageable = Pageable.ofSize(20);
        // When
        Page<ArticleDto> articles = sut.searchArticleDtosViaHashtag(null, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(hashtagRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("[예외] 해시태그 검색에 지정한 해시태그가 존재하지않는 해시태그이면 빈 페이지 반환")
    @Test
    void noSearchArticlesWithNotExistHashtag() {
        // Given
        String hashtagName = "null";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtagNames(Set.of(hashtagName), pageable)).willReturn(Page.empty(pageable));
        // When
        Page<ArticleDto> articles = sut.searchArticleDtosViaHashtag(hashtagName, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(hashtagRepository).shouldHaveNoInteractions();
        then(articleRepository).should().findByHashtagNames(Set.of(hashtagName), pageable);
    }

    @DisplayName("ID로 댓글 달린 게시글 조회")
    @Test
    void findArticleWithComments() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        // When
        ArticleWithCommentsDto dto = sut.getArticleWithCommentsDto(articleId);
        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );

        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("Page와 Index로 댓글 달린 게시글 조회")
    @Test
    void findArticleWithCommentsByPageIndex() {
        // Given
        int articleIndex = 0;
        int pageNumber = 0;
        int pageSize = 10;
        String sortName = "createdAt";
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortName));
        Article article = createArticle();
        given(articleRepository.findAll(pageable)).willReturn(new PageImpl(List.of(article), pageable, 1));
        // When
        ArticleWithCommentsDto dto = sut.getArticleWithCommentsDtoByPageIndex(articleIndex, pageable);
        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );

        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("[예외] ID로 존재하지 않는 댓글 달린 게시글 조회")
    @Test
    void findNotExistArticleWithComments() {
        // Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());
        // When

        // Then
        assertThatThrownBy(() -> sut.getArticleWithCommentsDto(articleId)).isInstanceOf(EntityNotFoundException.class);
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
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );

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

    @DisplayName("본문에서 해시태그를 추출하여 해시태그가 포함된 게시글을 저장")
    @Test
    void saveArticle() {
        // Given
        ArticleDto dto = createArticleDto("title", "content #java#spring");
        Set<String> expectedHashtagNames = Set.of("java", "spring");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        expectedHashtags.add(createHashtag("java"));
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(hashtagService.parseHashtagNames(dto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());
        // When
        sut.saveArticle(dto);
        // Then
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(hashtagService).should().parseHashtagNames(dto.content());
        then(hashtagService).should().findHashtagByNames(expectedHashtagNames);
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글 수정")
    @Test
    void updateArticle() {
        // Given
        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content #springboot");
        Set<String> expectedHashtagNames = Set.of("springboot");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(anyLong());
        given(hashtagService.parseHashtagNames(articleDto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        // When
        sut.updateArticle(articleDto.id(), articleDto);
        // Then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION))
                        .hasSize(1)
                        .extracting("hashtagName")
                        .containsExactly("springboot");
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
        then(hashtagService).should().parseHashtagNames(articleDto.content());
        then(hashtagService).should().findHashtagByNames(expectedHashtagNames);
    }

    @DisplayName("[예외] 없는 게시글 수정")
    @Test
    void updateNotExistArticle() {
        // Given
        ArticleDto articleDto = createArticleDto("new title", "new content");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateArticle(articleDto.id(), articleDto);
        // Then
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("[예외] 작성자와 수정자가 다를 경우 게시글 수정")
    @Test
    void updateArticleByNotMatchedUser() {
        // Given
        long differentArticleId = 22L;
        Article differentArticle = createArticle(differentArticleId);
        differentArticle.setUserAccount(createUserAccount("jade"));
        ArticleDto articleDto = createArticleDto("new title", "new content");
        given(articleRepository.getReferenceById(differentArticleId)).willReturn(differentArticle);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());
        // When
        sut.updateArticle(differentArticleId, articleDto);
        // Then
        then(articleRepository).should().getReferenceById(differentArticleId);
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 삭제")
    @Test
    void deleteArticle() {
        // Given
        Long articleId = 1L;
        String userId = "joo";
        given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(anyLong());
        // When
        sut.deleteArticle(articleId, userId);
        // Then
        then(articleRepository).should().getReferenceById(articleId);
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(anyLong());
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
        return createArticle(1L);
    }

    private Article createArticle(Long id) {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );
        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")
        ));
        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }

    private UserAccount createUserAccount() {
        return createUserAccount("joo");
    }

    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId,
                "pw",
                "joo@gmail.com",
                "joo",
                "memo"
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                null,
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

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long hashtagId, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", hashtagId);
        return hashtag;
    }
}
