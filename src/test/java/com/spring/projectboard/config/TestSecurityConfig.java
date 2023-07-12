package com.spring.projectboard.config;

import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.repository.UserAccountRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.Optional;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
public class TestSecurityConfig {
    @MockBean private UserAccountRepository userAccountRepository;

    @BeforeTestMethod
    public void securitySetUp() {
        given(userAccountRepository.findById(anyString()))
                .willReturn(
                        Optional.of(
                                UserAccount.of(
                                        "jooTest",
                                        "pw",
                                        "joo@gmail.com",
                                        "joo-test",
                                        "test memo")
                        )
                );
    }
}
