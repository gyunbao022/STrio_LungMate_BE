package com.example.strio01.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.strio01.user.entity.UserInfoEntity;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    // 로그인용 (ID로 사용자 조회)
    @Query("SELECT u FROM UserInfoEntity u WHERE u.userId = :userId")
    UserInfoEntity findByUserId(String userId);

    // 사용자 수 카운트
    @Query("SELECT COUNT(u) FROM UserInfoEntity u")
    long countUsers();
}
