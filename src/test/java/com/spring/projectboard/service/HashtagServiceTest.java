package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.Hashtag;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.repository.HashtagRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 해시태그")
@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {
    @InjectMocks private HashtagService sut;
    @Mock private HashtagRepository hashtagRepository;

    @DisplayName("본문에 있는 해시태그 파싱")
    @MethodSource
    @ParameterizedTest(name = "[{index}] {0} => {1}")
    void parseHashtagNamesTest(String input, Set<String> expected){
        // Given

        // When
        Set<String> actual = sut.parseHashtagNames(input);
        // Then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        then(hashtagRepository).shouldHaveNoInteractions();
    }

    static Stream<Arguments> parseHashtagNamesTest() {
        return Stream.of(
                arguments(null, Set.of()),
                arguments("", Set.of()),
                arguments("   ", Set.of()),
                arguments("#", Set.of()),
                arguments("  #", Set.of()),
                arguments("#   ", Set.of()),
                arguments("java", Set.of()),
                arguments("java#", Set.of()),
                arguments("ja#va", Set.of("va")),
                arguments("#java", Set.of("java")),
                arguments("#java_spring", Set.of("java_spring")),
                arguments("#java-spring", Set.of("java")),
                arguments("#_java_spring", Set.of("_java_spring")),
                arguments("#-java-spring", Set.of()),
                arguments("#_java_spring__", Set.of("_java_spring__")),
                arguments("#java#spring", Set.of("java", "spring")),
                arguments("#java #spring", Set.of("java", "spring")),
                arguments("#java  #spring", Set.of("java", "spring")),
                arguments("#java   #spring", Set.of("java", "spring")),
                arguments("#java     #spring", Set.of("java", "spring")),
                arguments("  #java     #spring ", Set.of("java", "spring")),
                arguments("   #java     #spring   ", Set.of("java", "spring")),
                arguments("#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring #부트", Set.of("java", "spring", "부트")),
                arguments("#java,#spring,#부트", Set.of("java", "spring", "부트")),
                arguments("#java.#spring;#부트", Set.of("java", "spring", "부트")),
                arguments("#java|#spring:#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring  #부트", Set.of("java", "spring", "부트")),
                arguments("   #java,? #spring  ...  #부트 ", Set.of("java", "spring", "부트")),
                arguments("#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring#java#부트#java", Set.of("java", "spring", "부트")),
                arguments("#java#스프링 아주 긴 글~~~~~~~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~~~~~~~~~~~~~~~~#java#스프링", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java#스프링~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java~~~~~~~#스프링~~~~~~~~", Set.of("java", "스프링"))
        );
    }

    @DisplayName("해시태그들 중 저장된 해시태그들 반환")
    @Test
    void findHashtagByNamesTest() {
        // Given
        Set<String> hashtagNames = Set.of("java", "spring", "boot");
        List<Hashtag> expectedHashtags = List.of(Hashtag.of("java"), Hashtag.of("spring"));
        given(hashtagRepository.findByHashtagNameIn(hashtagNames)).willReturn(expectedHashtags);
        // When
        Set<Hashtag> hashtags = sut.findHashtagByNames(hashtagNames);
        // Then
        assertThat(hashtags).containsExactlyInAnyOrderElementsOf(expectedHashtags);
        then(hashtagRepository).should().findByHashtagNameIn(hashtagNames);
    }

    @DisplayName("해시태그 리스트 조회")
    @Test
    void getHashtagsTest(){
        // Given
        List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
        given(hashtagRepository.findAllHashtagNames()).willReturn(expectedHashtags);
        // When
        List<String> actualHashtags = sut.getHashtags();
        // Then
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(hashtagRepository).should().findAllHashtagNames();
    }

    @DisplayName("해시태그 삭제")
    @Test
    void deleteHashtag() {
        // Given
        long hashtagId = 1L;
        Hashtag hashtag = createHashtag();
        given(hashtagRepository.getReferenceById(hashtagId)).willReturn(hashtag);
        willDoNothing().given(hashtagRepository).delete(hashtag);
        // When
        sut.deleteHashtagWithoutArticles(hashtagId);
        // Then
        then(hashtagRepository).should().getReferenceById(hashtagId);
        then(hashtagRepository).should().delete(hashtag);
    }

    @DisplayName("게시글을 가지는 해시태그는 삭제하지 않음")
    @Test
    void deleteHashtagNotEmptyArticles() {
        // Given
        long hashtagId = 1L;
        String expectedHashtagName = "java";
        Hashtag hashtag = createHashtagWithArticles();
        given(hashtagRepository.getReferenceById(hashtagId)).willReturn(hashtag);
        // When
        sut.deleteHashtagWithoutArticles(hashtagId);
        // Then
        then(hashtagRepository).should().getReferenceById(hashtagId);
        then(hashtagRepository).should(times(0)).delete(hashtag);
    }

    private Hashtag createHashtag() {
        return Hashtag.of("java");
    }

    private Hashtag createHashtagWithArticles() {
        Hashtag hashtag = Hashtag.of("java");
        hashtag.getArticles().add(createArticle());
        return hashtag;
    }

    private Article createArticle() {
        return Article.of(createUserAccount(), "title", "content");
    }

    private UserAccount createUserAccount() {
        return UserAccount.of("joo", "pw", "joo@gmail.com", "joo", "memo");
    }
}
