package com.spring.projectboard.dto.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO - Kakao OAuth 2.0 인증 응답 데이터 테스트")
class KakaoOauth2ResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @DisplayName("인증 결과를 Map(Deserialized JSON)으로 받으면 KakaoOauth2Response로 변환")
    @Test
    void mapfromJsonToKakaoOauth2Response() throws JsonProcessingException {
        // Given
        String serializedResponse = """
                {
                    "id": 1234567890,
                    "connected_at": "2022-01-02T00:12:34Z",
                    "kakao_account": {
                        "profile_nickname_needs_agreement": false,
                        "profile": {
                            "nickname": "홍길동"
                        },
                        "email_needs_agreement": false,
                        "is_email_valid": true,
                        "is_email_verified": true,
                        "email": "test@gmail.com"
                    }
                }
                """;
        Map<String, Object> attributes = mapper.readValue(serializedResponse, new TypeReference<>() {});
        // When
        KakaoOauth2Response result = KakaoOauth2Response.from(attributes);
        // Then
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", 1234567890L)
                .hasFieldOrPropertyWithValue(
                        "connectedAt",
                        ZonedDateTime.of(2022,
                                        1,
                                        2,
                                        0,
                                        12,
                                        34,
                                        0,
                                        ZoneOffset.UTC
                                )
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()
                )
                .hasFieldOrPropertyWithValue("kakaoAccount.profile.nickname", "홍길동")
                .hasFieldOrPropertyWithValue("kakaoAccount.email", "test@gmail.com");
    }
}
