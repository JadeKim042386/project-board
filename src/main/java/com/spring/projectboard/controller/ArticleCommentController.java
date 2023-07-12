package com.spring.projectboard.controller;

import com.spring.projectboard.dto.security.BoardPrincipal;
import com.spring.projectboard.request.ArticleCommentRequest;
import com.spring.projectboard.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {
    private final ArticleCommentService articleCommentService;

    @PostMapping("/new")
    public String postNewComment(
            ArticleCommentRequest articleCommentRequest,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        articleCommentService.saveComment(articleCommentRequest.toDto(boardPrincipal.toDto()));
        return "redirect:/articles/" + articleCommentRequest.articleId();
    }

    @PostMapping("{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        articleCommentService.deleteComment(commentId, boardPrincipal.getUsername());
        return "redirect:/articles/" + articleId;
    }
}
