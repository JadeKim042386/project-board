package com.spring.projectboard.service;

import com.spring.projectboard.domain.Hashtag;
import com.spring.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    /**
     * 본문에서 해시태그 파싱
     */
    @Transactional(readOnly = true)
    public Set<String> parseHashtagNames(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }
        Pattern pattern = Pattern.compile("#[\\w가-힣]+");
        Matcher matcher = pattern.matcher(content);
        Set<String> result = new HashSet<>();

        while (matcher.find()) {
            result.add(matcher.group().replace("#", ""));
        }

        return Set.copyOf(result);
    }

    /**
     * 해시태그들 중 DB에 저장된 해시태그들 반환
     */
    @Transactional(readOnly = true)
    public Set<Hashtag> findHashtagByNames(Set<String> hashtagNames) {
        return new HashSet<>(hashtagRepository.findByHashtagNameIn(hashtagNames));
    }

    /**
     * 어떤 게시글에도 가지지않는 해시태그 삭제
     */
    public void deleteHashtagWithoutArticles(Long hashtagId) {
        Hashtag hashtag = hashtagRepository.getReferenceById(hashtagId);
        if (hashtag.getArticles().isEmpty()) {
            hashtagRepository.delete(hashtag);
        }
    }

    public List<String> getHashtags() {
        return hashtagRepository.findAllHashtagNames();
    }
}
