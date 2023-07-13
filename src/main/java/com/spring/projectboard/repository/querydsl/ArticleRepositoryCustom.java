package com.spring.projectboard.repository.querydsl;

import com.spring.projectboard.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ArticleRepositoryCustom {
    /**
     * @deprecated 해시태그 도메인을 새로 만들었으므로 사용할 필요 없다.
     * @see HashtagRepositoryCustom#findALlHashtagNames()
     */
    @Deprecated
    List<String> findAllDistinctHashtags();
    Page<Article> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable);
}
