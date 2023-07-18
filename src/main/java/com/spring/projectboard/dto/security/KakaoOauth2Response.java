package com.spring.projectboard.dto.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info-response
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#kakaoaccount
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#profile
 */
@SuppressWarnings("unchecked") //TODO: 제네릭 타입 캐스팅 문제
public record KakaoOauth2Response(
        Long id,
        LocalDateTime connectedAt,
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            Boolean profileNicknameNeedsAgreement, //사용자 동의 시 닉네임 제공 가능
            Profile profile, //프로필 정보
            Boolean emailNeedsAgreement, //사용자 동의 시 카카오계정 대표 이메일 제공 가능
            Boolean isEmailValid, //이메일 유효 여부
            Boolean isEmailVerified, //이메일 인증 여부
            String email //카카오계정 대표 이메일
    ){
        public record Profile(String nickname){
            public static Profile from(Map<String, Object> attributes) {
                return new Profile(attributes.get("nickname").toString());
            }
        }

        public static KakaoAccount from(Map<String, Object> attributes) {
            return new KakaoAccount(
                    Boolean.valueOf(String.valueOf(attributes.get("profile_nickname_needs_agreement"))),
                    Profile.from((Map<String, Object>) attributes.get("profile")),
                    Boolean.valueOf(String.valueOf(attributes.get("email_needs_agreement"))),
                    Boolean.valueOf(String.valueOf(attributes.get("is_email_valid"))),
                    Boolean.valueOf(String.valueOf(attributes.get("is_email_verified"))),
                    String.valueOf(attributes.get("email"))
            );
        }

        public String nickname() { return this.profile().nickname(); }
    }

    public static KakaoOauth2Response from(Map<String, Object> attributes) {
        return new KakaoOauth2Response(
                Long.valueOf(String.valueOf(attributes.get("id"))),
                LocalDateTime.parse(
                        attributes.get("connected_at").toString(),
                        DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
                ),
                KakaoAccount.from((Map<String, Object>) attributes.get("kakao_account"))
        );
    }

    public String email() { return this.kakaoAccount().email(); }
    public String nickname() { return this.kakaoAccount().nickname(); }
}
