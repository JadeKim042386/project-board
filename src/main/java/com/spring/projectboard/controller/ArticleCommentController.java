package com.spring.projectboard.controller;

import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.request.ArticleCommentRequest;
import com.spring.projectboard.request.ArticleRequest;
import com.spring.projectboard.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
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
    public String postNewComment(ArticleCommentRequest articleCommentRequest) {
        // TODO: 인증 정보 추가
        articleCommentService.saveComment(articleCommentRequest.toDto(UserAccountDto.of("joo", "pw", "joo@gamil.com", "joo", "memo")));
        return "redirect:/articles/" + articleCommentRequest.articleId();
    }

    @PostMapping("{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId, Long articleId) {
        articleCommentService.deleteComment(commentId);
        return "redirect:/articles/" + articleId;
    }
}
