package com.spring.projectboard.dto;

import java.time.LocalDateTime;

public record UserAccountDto(
        Long id,
        Long userId,
        String email,
        String nickname,
        String memo,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static UserAccountDto of(Long id, Long userId, String email, String nickname, String memo, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new UserAccountDto(id, userId, email, nickname, memo, createdAt, createdBy, modifiedAt, modifiedBy);
    }
}
