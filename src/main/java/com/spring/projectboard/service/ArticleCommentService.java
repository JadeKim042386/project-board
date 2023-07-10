package com.spring.projectboard.service;

import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.repository.ArticleCommentRepository;
import com.spring.projectboard.repository.ArticleRepository;
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
    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> getComments(Long articleId) {
        return articleCommentRepository.findByArticleId(articleId)
                .stream()
                .map(ArticleCommentDto::from)
                .toList();
    }

    public void saveComment(ArticleCommentDto dto) {
        try{
            articleCommentRepository.save(dto.toEntity(articleRepository.getReferenceById(dto.articleId())));
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패! 게시글을 찾을 수 없습니다. - dto: {}", dto);
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

    public void deleteComment(Long commentId) {
        articleCommentRepository.deleteById(commentId);
    }
}
