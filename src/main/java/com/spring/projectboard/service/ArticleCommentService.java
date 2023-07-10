package com.spring.projectboard.service;

import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.repository.ArticleCommentRepository;
import com.spring.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return List.of();
    }

    public void saveComment(ArticleCommentDto dto) {
    }

    public void updateComment(ArticleCommentDto dto) {
    }

    public void deleteComment(Long commentId) {
    }
}
