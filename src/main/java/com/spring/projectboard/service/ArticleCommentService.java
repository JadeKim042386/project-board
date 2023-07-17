package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.repository.ArticleCommentRepository;
import com.spring.projectboard.repository.ArticleRepository;
import com.spring.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ArticleCommentService {
    private final UserAccountRepository userAccountRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> getComments(Long articleId) {
        return articleCommentRepository.findByArticleId(articleId)
                .stream()
                .map(ArticleCommentDto::from)
                .toList();
    }

    /**
     * 부모 댓글 ID가 존재하면 자식 댓글이므로 부모 댓글의 자식 댓글 collection 에 추가, null 이면 부모 댓글 추가
     */
    public void saveComment(ArticleCommentDto dto) {
        try{
            Article article = articleRepository.getReferenceById(dto.articleId());
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
            ArticleComment articleComment = dto.toEntity(article, userAccount);
            if (dto.parentCommentId() != null) {
                ArticleComment parentComment = articleCommentRepository.getReferenceById(dto.parentCommentId());
                parentComment.addChildComment(articleComment);
            } else {
                articleCommentRepository.save(articleComment);
            }

        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패! 댓글 작성에 필요한 정보를 찾을 수 없습니다. - {}", e);
        }
    }

    public void updateComment(ArticleCommentDto dto) {
        try {
            ArticleComment articleComment = articleCommentRepository.getReferenceById(dto.id());
            if (dto.content() != null) {
                articleComment.setContent(dto.content());
            }
        } catch (EntityNotFoundException e) {
            log.warn("댓글 수정 실패! 댓글을 찾을 수 없습니다. - dto: {}", dto);
        }
    }

    public void deleteComment(Long commentId, String userId) {
        articleCommentRepository.deleteByIdAndUserAccount_UserId(commentId, userId);
    }
}
