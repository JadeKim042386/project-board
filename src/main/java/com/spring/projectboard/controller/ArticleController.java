package com.spring.projectboard.controller;

import com.spring.projectboard.domain.constant.SearchType;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.response.ArticleResponse;
import com.spring.projectboard.dto.response.ArticleWithCommentResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String article(
            @PathVariable Long articleId,
            Model model) {
        ArticleWithCommentResponse article = ArticleWithCommentResponse.from(articleService.getArticle(articleId));

        model.addAttribute("article", article);
        model.addAttribute("articleComments", article.articleCommentResponses());
        return "articles/detail";
    }
}
