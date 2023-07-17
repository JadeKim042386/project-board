package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.Hashtag;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.repository.ArticleRepository;
import com.spring.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final HashtagService hashtagService;
    private final UserAccountRepository userAccountRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }
        return switch (searchType) {
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
            case HASHTAG -> articleRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).toList(), pageable).map(ArticleDto::from);
        };
    }

    @Deprecated
    @Transactional(readOnly = true)
    public ArticleDto getArticle(Long articleId) {
        return articleRepository.findById(articleId).map(ArticleDto::from).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    @Transactional(readOnly = true)
    public ArticleDto getArticleDtoByPageIndex(int articleIndex, Pageable pageable) {
        try {
            return ArticleDto.from(getArticleByPageIndex(articleIndex, pageable));
        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    @Deprecated
    @Transactional(readOnly = true)
    public ArticleWithCommentsDto getArticleWithCommentsDto(Long articleId){
        return articleRepository.findById(articleId).map(ArticleWithCommentsDto::from).orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - articleId: " + articleId));
    }

    @Transactional(readOnly = true)
    public ArticleWithCommentsDto getArticleWithCommentsDtoByPageIndex(int articleIndex, Pageable pageable) {
        try {
            return ArticleWithCommentsDto.from(getArticleByPageIndex(articleIndex, pageable));
        } catch (EntityNotFoundException e) {
            throw e;
        }
    }

    /**
     * 본문에서 해시태그를 추출하고 추출한 해시태그를 가지는 게시글을 저장
     */
    public void saveArticle(ArticleDto dto) {
        UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
        Set<Hashtag> hashtags = renewHashtagsFromContent(dto.content());

        Article article = dto.toEntity(userAccount);
        article.addHashtags(hashtags);

        articleRepository.save(article);
    }

    public void updateArticle(Long articleId, ArticleDto articleDto) {
        try {
            Article article = articleRepository.getReferenceById(articleId);
            UserAccount userAccount = userAccountRepository.getReferenceById(articleDto.userAccountDto().userId());

            //작성자와 수정자가 같은지 확인
            if (article.getUserAccount().equals(userAccount)) {
                if (articleDto.title() != null) {
                    article.setTitle(articleDto.title());
                }
                if (articleDto.content() != null) {
                    article.setContent(articleDto.content());
                }

                //수정 후 flush
                Set<Long> hashtagIds = article.getHashtags().stream()
                        .map(Hashtag::getId)
                        .collect(Collectors.toUnmodifiableSet());
                article.clearHashtags();
                articleRepository.flush();

                //어떤 게시글에도 없는 해시태그 삭제
                hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);

                //업데이트된 본문에서 해시태그를 파싱하여 추가
                Set<Hashtag> hashtags = renewHashtagsFromContent(articleDto.content());
                article.addHashtags(hashtags);
            }
        } catch (EntityNotFoundException e) {
            log.warn("게시글 업데이트 실패! 게시글을 수정하는데 필요한 정보를 찾을 수 없습니다 - {}", e);
        }
    }

    public void deleteArticle(Long articleId, String userId) {
        Article article = articleRepository.getReferenceById(articleId);
        Set<Long> hashtagIds = article.getHashtags().stream()
                .map(Hashtag::getId)
                .collect(Collectors.toUnmodifiableSet());

        articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId);
        articleRepository.flush();

        hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
    }

    @Transactional(readOnly = true)
    public long getArticleCount() {
        return articleRepository.count();
    }

    /**
     * 주어진 해시태그를 가지는 게시글들을 조회
     */
    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticleDtosViaHashtag(String hashtagName, Pageable pageable) {
        if (hashtagName == null || hashtagName.isBlank()) {
            return Page.empty(pageable);
        }
        return articleRepository.findByHashtagNames(Set.of(hashtagName), pageable).map(ArticleDto::from);
    }

    /**
     * 본문에서 해시태그를 파싱하고 이미 DB에 존재하고 있는 해시태그를 제외한 해시태그들만 새로 만들어서(새로운 ID) 추가
     */
    private Set<Hashtag> renewHashtagsFromContent(String content) {
        Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content);
        Set<Hashtag> hashtags = hashtagService.findHashtagByNames(hashtagNamesInContent);

        Set<String> existingHashtagNames = hashtags.stream()
                .map(Hashtag::getHashtagName)
                .collect(Collectors.toUnmodifiableSet());

        hashtagNamesInContent.forEach(newHashtagName -> {
            if (!existingHashtagNames.contains(newHashtagName)) {
                hashtags.add(Hashtag.of(newHashtagName));
            }
        });

        return hashtags;
    }

    @Transactional(readOnly = true)
    public Article getArticleByPageIndex(int articleIndex, Pageable pageable) {
        List<Article> articles = articleRepository.findAll(pageable).getContent();
        return articles.get(articleIndex);
    }
}
