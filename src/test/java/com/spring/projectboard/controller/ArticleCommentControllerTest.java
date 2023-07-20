package com.spring.projectboard.controller;

import com.spring.projectboard.config.TestSecurityConfig;
import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.request.ArticleCommentRequest;
import com.spring.projectboard.service.ArticleCommentService;
import com.spring.projectboard.util.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 댓글")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(ArticleCommentController.class)
class ArticleCommentControllerTest {
    private final MockMvc mvc;
    private final FormDataEncoder formDataEncoder;

    @MockBean private ArticleCommentService articleCommentService;

    public ArticleCommentControllerTest(@Autowired MockMvc mvc, @Autowired FormDataEncoder formDataEncoder) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }

    @WithUserDetails(value = "jooTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 댓글 등록 - 정상 호출")
    @Test
    void saveNewComment() throws Exception {
        // Given
        long articleId = 1L;
        int articleIndex = 0;
        long articleCommentId = 1L;
        Pageable pageable = Pageable.ofSize(10);
        ArticleCommentRequest request = ArticleCommentRequest.of(articleId, "test comment");
        given(articleCommentService.saveComment(any(ArticleCommentDto.class))).willReturn(articleCommentId);
        // When & Then
        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(request))
                                .queryParam("articleIndex", String.valueOf(articleIndex))
                                .queryParam("page", String.valueOf(pageable.getPageNumber()))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/detail?articleIndex=0&page=0#comment" + articleCommentId))
                .andExpect(redirectedUrl("/articles/detail?articleIndex=0&page=0#comment" + articleCommentId));
        then(articleCommentService).should().saveComment(any(ArticleCommentDto.class));
    }

    @WithUserDetails(value = "jooTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 댓글 삭제 - 정상 호출")
    @Test
    void deleteComment() throws Exception {
        // Given
        long articleId = 1L;
        long articleCommentId = 1L;
        String userId = "jooTest";
        willDoNothing().given(articleCommentService).deleteComment(articleCommentId, userId);
        // When & Then
        mvc.perform(
                        post("/comments/" + articleCommentId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(Map.of("articleId", articleId)))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(articleCommentService).should().deleteComment(articleCommentId, userId);
    }

    @WithUserDetails(value = "jooTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 대댓글 등록 - 정상 호출")
    @Test
    void addChildComment() throws Exception {
        // Given
        long articleId = 1L;
        long parentCommentId = 1L;
        long articleCommentId = 1L;
        ArticleCommentRequest request = ArticleCommentRequest.of(articleId, parentCommentId, "test content");
        given(articleCommentService.saveComment(any(ArticleCommentDto.class))).willReturn(articleCommentId);
        // When & Then
        mvc.perform(
                post("/comments/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .queryParam("articleIndex", String.valueOf(0))
                        .queryParam("pageable", String.valueOf(Pageable.ofSize(10)))
                        .content(formDataEncoder.encode(request))
                        .with(csrf())
            )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/detail?articleIndex=0&page=0#comment" + articleCommentId))
                .andExpect(redirectedUrl("/articles/detail?articleIndex=0&page=0#comment" + articleCommentId));
        then(articleCommentService).should().saveComment(any(ArticleCommentDto.class));
    }
}
