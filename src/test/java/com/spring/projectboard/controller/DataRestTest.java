package com.spring.projectboard.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@DisplayName("Data REST - API 테스트")
public class DataRestTest {
    private final MockMvc mvc;

    public DataRestTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @Test
    @DisplayName("[api] 게시글 리스트 조회")
    void requestArticles() throws Exception {
        // Given

        // When
        mvc.perform(get("/api/articles"))
                .andExpect(status().isOk()) //200
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andDo(print());
    }

    @Test
    @DisplayName("[api] 게시글 단일 조회")
    void requestArticle() throws Exception {
        // Given

        // When
        mvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk()) //200
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andDo(print());
    }

    @Test
    @DisplayName("[api] 댓글 리스트 조회")
    void requestArticleComments() throws Exception {
        // Given

        // When
        mvc.perform(get("/api/articleComments"))
                .andExpect(status().isOk()) //200
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andDo(print());
    }

    @Test
    @DisplayName("[api] 댓글 단일 조회")
    void requestArticleComment() throws Exception {
        // Given

        // When
        mvc.perform(get("/api/articleComments/1"))
                .andExpect(status().isOk()) //200
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andDo(print());
    }
}
