package com.spring.projectboard.controller;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.constant.FormStatus;
import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.dto.response.ArticleResponse;
import com.spring.projectboard.dto.response.ArticleWithCommentResponse;
import com.spring.projectboard.request.ArticleRequest;
import com.spring.projectboard.service.ArticleService;
import com.spring.projectboard.service.PaginationService;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /articles
 * /articles/{article-id}
 * /articles/search
 * /articles/search-hashtag
 */
@RequiredArgsConstructor
@RequestMapping("articles")
@Controller
public class ArticleController {
    private final ArticleService articleService;
    private final PaginationService paginationService;
    @GetMapping
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        model.addAttribute("articles", articles);
        model.addAttribute("paginationBarNumbers", barNumbers);
        model.addAttribute("searchTypes", SearchType.values());

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String article(
            @PathVariable Long articleId,
            Model model) {
        ArticleWithCommentResponse article = ArticleWithCommentResponse.from(articleService.getArticleWithComments(articleId));

        model.addAttribute("article", article);
        model.addAttribute("articleComments", article.articleCommentResponses());
        model.addAttribute("totalCount", articleService.getArticleCount());
        return "articles/detail";
    }

    @GetMapping("/search-hashtag")
    public String searchHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
        List<String> hashtags = articleService.getHashtags();

        model.addAttribute("articles", articles);
        model.addAttribute("hashtags", hashtags);
        model.addAttribute("paginationBarNumbers", barNumbers);
        model.addAttribute("searchType", SearchType.HASHTAG);

        return "articles/search-hashtag";
    }

    @GetMapping("/form")
    public String articleForm(Model model) {
        model.addAttribute("formStatus", FormStatus.CREATE);
        return "articles/form";
    }

    @PostMapping("/form")
    public String postNewArticle(ArticleRequest articleRequest) {
        //TODO: 인증 정보 추가
        articleService.saveArticle(
                articleRequest.toDto(
                        UserAccountDto.of(
                                "joo",
                                "pw",
                                "joo@gmail.com",
                                "Joo",
                                "memo",
                                null,
                                null,
                                null,
                                null)
                )
        );
        return "redirect:/articles";
    }

    @GetMapping("/{articleId}/form")
    public String updateArticle(@PathVariable Long articleId, Model model) {
        ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));

        model.addAttribute("article", article);
        model.addAttribute("formStatus", FormStatus.UPDATE);

        return "articles/form";
    }

    @PostMapping("/{articleId}/form")
    public String postUpdateArticle(@PathVariable Long articleId, ArticleRequest articleRequest) {
        //TODO: 인증 정보 추가
        articleService.updateArticle(
                articleId,
                articleRequest.toDto(
                        UserAccountDto.of(
                                "joo",
                                "pw",
                                "joo@gmail.com",
                                "Joo",
                                "memo",
                                null,
                                null,
                                null,
                                null)
                )
        );
        return "redirect:/articles/" + articleId;
    }

    @PostMapping("{articleId}/delete")
    public String deleteArticle(@PathVariable Long articleId) {
        //TODO: 인증 정보 추가
        articleService.deleteArticle(articleId);
        return "redirect:/articles";
    }
}
