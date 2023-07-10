package com.spring.projectboard.domain.constant;

import lombok.Getter;

@Getter
public enum SearchType {
    TITLE("제목"),
    CONTENT("본문"),
    HASHTAG("해시태그"),
    ID("유저 ID"),
    NICKNAME("닉네임");
    private final String description;

    SearchType(String description) {
        this.description = description;
    }
}
