package com.spring.projectboard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class PaginationService {
    private static final int BAR_LENGTH = 5;
    public List<Integer> getPaginationBarNumbers(int currentPageNumber, int totalPages) {
        int startNumber = Math.max(currentPageNumber - (BAR_LENGTH / 2), 0);
        int endNumber = Math.min(startNumber + BAR_LENGTH, totalPages);

        return IntStream.range(startNumber, endNumber).boxed().toList();
    }

    public int currentBarLength() {
        return BAR_LENGTH;
    }

    public String getPreviousUri(int articleIndex, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        UriComponents prevUri = UriComponentsBuilder.newInstance().path("#").build();

        if (pageNumber == 0 && articleIndex == 0){
            return prevUri.toUriString();
        } else {
            if (pageNumber > 0 && articleIndex == 0) {
                articleIndex = pageSize - 1;
                pageNumber -= 1;
            } else {
                articleIndex -= 1;
            }
        }

        prevUri = UriComponentsBuilder.newInstance().path("/articles/detail")
                .queryParam("articleIndex", articleIndex)
                .queryParam("page", pageNumber)
                .build();
        return prevUri.toUriString();
    }

    public String getNextUri(int articleIndex, Pageable pageable, long articleTotal) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int lastPage = (int) articleTotal / pageSize;
        int lastIndex = (int) articleTotal - (lastPage * pageSize) - 1;
        UriComponents nextUri = UriComponentsBuilder.newInstance().path("#").build();
        if (pageNumber == lastPage && articleIndex == lastIndex) {
            return nextUri.toUriString();
        } else {
            if (pageNumber < lastPage && articleIndex == pageSize - 1) {
                nextUri = UriComponentsBuilder.newInstance().path("/articles/detail")
                        .queryParam("articleIndex", 0)
                        .queryParam("page", pageNumber + 1)
                        .build();
            } else {
                nextUri = UriComponentsBuilder.newInstance().path("/articles/detail")
                        .queryParam("articleIndex", articleIndex + 1)
                        .queryParam("page", pageNumber)
                        .build();
            }
        }
        return nextUri.toUriString();
    }
}
