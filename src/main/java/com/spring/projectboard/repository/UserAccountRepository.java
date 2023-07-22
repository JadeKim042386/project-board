package com.spring.projectboard.repository;

import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.domain.projection.ArticleProjection;
import com.spring.projectboard.domain.projection.UserAccountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = UserAccountProjection.class)
public interface UserAccountRepository extends JpaRepository<UserAccount, String>{
}
