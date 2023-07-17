package com.spring.projectboard.repository;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.domain.Hashtag;
import com.spring.projectboard.domain.UserAccount;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {
    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;
    private final HashtagRepository hashtagRepository;

    public JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired ArticleCommentRepository articleCommentRepository,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired HashtagRepository hashtagRepository) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.userAccountRepository = userAccountRepository;
        this.hashtagRepository = hashtagRepository;
    }

    @Test
    @DisplayName("select 테스트")
    void givenTestData_whenSelecting_thenWorksFile() {
        // Given

        // When
        List<Article> articles = articleRepository.findAll();
        // Then
        assertThat(articles).isNotNull().hasSize(123);
    }

    @Test
    @DisplayName("insert 테스트")
    void givenTestData_whenInserting_thenWorksFile() {
        // Given
        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("jujoo", "pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content");
        article.addHashtags(Set.of(Hashtag.of("spring")));
        // When
        articleRepository.save(article);
        // Then
        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @Test
    @DisplayName("update 테스트")
    @Rollback(value = false)
    void givenTestData_whenUpdating_thenWorksFile() {
        // Given
        Article article = articleRepository.findById(1L).orElseThrow();
        Hashtag updatedHashtag = Hashtag.of("springboot");
        article.clearHashtags();
        article.addHashtag(updatedHashtag);
        // When
        Article savedArticle = articleRepository.saveAndFlush(article);
        // Then
        assertThat(savedArticle.getHashtags())
                .hasSize(1)
                .extracting("hashtagName", String.class)
                .containsExactly(updatedHashtag.getHashtagName());
    }

    @Test
    @DisplayName("delete 테스트")
    void givenTestData_whenDeleting_thenWorksFile() {
        // Given
        Article article = articleRepository.findById(1L).orElseThrow();
        long previousArticleCount = articleRepository.count();
        long previousArticleCommentCount = articleCommentRepository.count();
        int deletedCommentsSize = article.getArticleComments().size();

        // When
        articleRepository.delete(article);
        // Then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - deletedCommentsSize);
    }

    @DisplayName("대댓글 조회")
    @Test
    void getChildComments() {
        // Given
        long articleCommentId = 1L;
        // When
        Optional<ArticleComment> parentComment = articleCommentRepository.findById(articleCommentId);
        // Then
        assertThat(parentComment).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(4);
    }

    @DisplayName("대댓글 추가")
    @Test
    void addChildComment() {
        // Given
        long articleCommentId = 1L;
        ArticleComment parentComment = articleCommentRepository.getReferenceById(articleCommentId);
        ArticleComment childComment = ArticleComment.of(
                parentComment.getUserAccount(),
                parentComment.getArticle(),
                "대댓글"
        );
        // When
        parentComment.addChildComment(childComment);
        articleCommentRepository.flush();
        // Then
        assertThat(articleCommentRepository.findById(articleCommentId)).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(5);
    }

    @DisplayName("댓글과 대댓글 삭제")
    @Test
    void deleteCommentWithChildComments() {
        // Given
        long articleCommentId = 1L;
        ArticleComment parentComment = articleCommentRepository.getReferenceById(articleCommentId);
        long previousArticleCommentCount = articleCommentRepository.count();
        // When
        articleCommentRepository.delete(parentComment);
        // Then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - 5);
    }

    @DisplayName("댓글과 대댓글 삭제 by 댓글 ID + 유저 ID")
    void deleteCommentWithChildCommentsByCommentIdAndUserID() {
        // Given
        long articleCommentId = 1L;
        String userId = "joo";
        long previousArticleCommentCount = articleCommentRepository.count();
        // When
        articleCommentRepository.deleteByIdAndUserAccount_UserId(articleCommentId, userId);
        // Then
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - 5);
    }

    @DisplayName("[Querydsl] 전체 hashtag 리스트에서 이름만 조회")
    @Test
    void givenNothing_whenQueryingHashtags_thenReturnsHashtagNames() {
        // Given

        // When
        List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

        // Then
        assertThat(hashtagNames).hasSize(20);
    }

    @DisplayName("[Querydsl] hashtag로 페이징된 게시글 검색")
    @Test
    void givenHashtagNamesAndPageable_whenQueryingArticles_thenReturnsArticlePage() {
        // Given
        List<String> hashtagNames = List.of("blue", "crimson", "fuscia");
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
                Sort.Order.desc("hashtags.hashtagName"),
                Sort.Order.asc("title")
        ));

        // When
        Page<Article> articlePage = articleRepository.findByHashtagNames(hashtagNames, pageable);

        // Then
        assertThat(articlePage.getContent()).hasSize(pageable.getPageSize());
        assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Duis bibendum.");
        assertThat(articlePage.getContent().get(0).getHashtags())
                .extracting("hashtagName", String.class)
                .containsExactly("fuscia");
        assertThat(articlePage.getTotalElements()).isEqualTo(5);
        assertThat(articlePage.getTotalPages()).isEqualTo(1);
    }

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("joo");
        }
    }
}
