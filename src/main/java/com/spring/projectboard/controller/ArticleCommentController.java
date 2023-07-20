package com.spring.projectboard.controller;

import com.spring.projectboard.dto.security.BoardPrincipal;
import com.spring.projectboard.request.ArticleCommentRequest;
import com.spring.projectboard.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {
    private final ArticleCommentService articleCommentService;

    @PostMapping("/new")
    public String postNewComment(
            ArticleCommentRequest articleCommentRequest,
            @RequestParam int articleIndex,
            @PageableDefault(size=10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal) {
        Long articleCommentId = articleCommentService.saveComment(articleCommentRequest.toDto(boardPrincipal.toDto()));

        return UriComponentsBuilder.newInstance()
                .path("redirect:/articles/detail")
                .queryParam("articleIndex", articleIndex)
                .queryParam("page", pageable.getPageNumber())
                .build().toUriString() + "#comment" + articleCommentId;
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
