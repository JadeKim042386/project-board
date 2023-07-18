package com.spring.projectboard.service;

import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 회원")
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @InjectMocks private UserAccountService sut;
    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("존재하는 회원 ID를 검색하면 회원 데이터를 Optional로 반환")
    @Test
    void existingUserIdToOptional() {
        // Given
        String username = "joo";
        given(userAccountRepository.findById(username))
                .willReturn(Optional.of(createUserAccount(username)));
        // When
        Optional<UserAccountDto> result = sut.searchUser(username);
        // Then
        assertThat(result).isPresent();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("존재하지 않는 회원 ID를 검색하면 비어있는 Optional을 반환")
    @Test
    void notExistingUserIdToOptional() {
        // Given
        String username = "wrong-user";
        given(userAccountRepository.findById(username))
                .willReturn(Optional.empty());
        // When
        Optional<UserAccountDto> result = sut.searchUser(username);
        // Then
        assertThat(result).isEmpty();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("회원 정보를 입력하면 새로운 회원 정보를 저장하고 해당 회원 데이터를 반환")
    @Test
    void saveUserAccountAndReturn() {
        // Given
        UserAccount userAccount = createUserAccount("joo");
        UserAccount savedUserAccount = createSigningUpUserAccount("joo");
        given(userAccountRepository.save(userAccount)).willReturn(savedUserAccount);
        // When
        UserAccountDto result = sut.saveUser(
                userAccount.getUserId(),
                userAccount.getUserPassword(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getMemo()
        );
        // Then
        assertThat(result)
                .hasFieldOrPropertyWithValue("userId", userAccount.getUserId())
                .hasFieldOrPropertyWithValue("userPassword", userAccount.getUserPassword())
                .hasFieldOrPropertyWithValue("email", userAccount.getEmail())
                .hasFieldOrPropertyWithValue("nickname", userAccount.getNickname())
                .hasFieldOrPropertyWithValue("memo", userAccount.getMemo())
                .hasFieldOrPropertyWithValue("createdBy", userAccount.getUserId())
                .hasFieldOrPropertyWithValue("modifiedBy", userAccount.getUserId());
        then(userAccountRepository).should().save(userAccount);
    }


    private UserAccount createSigningUpUserAccount(String username) {
        return createUserAccount(username, username);
    }

    private UserAccount createUserAccount(String username) {
        return createUserAccount(username, null);
    }

    private UserAccount createUserAccount(String username, String createdBy) {
        return UserAccount.of(
                username,
                "pw",
                "test@gmail.com",
                "nickname",
                "memo",
                createdBy
        );
    }
}