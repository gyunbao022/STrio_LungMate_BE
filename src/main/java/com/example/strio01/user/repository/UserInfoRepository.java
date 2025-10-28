package com.example.strio01.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.strio01.user.entity.UserInfoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.strio01.user.entity.UserInfoEntity;
import org.springframework.data.repository.query.Param; // ✅ 추가

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {
    // 로그인용 (ID로 사용자 조회)
    Optional<UserInfoEntity> findByUserNameAndEmail(String userName, String email);
    
    // 로그인용 (ID로 사용자 조회)
    Optional<UserInfoEntity> findByUserIdAndEmail(String userId, String email);       
    
    @Query("SELECT u FROM UserInfoEntity u WHERE u.userId = :userId")
    UserInfoEntity findByUserId(@Param("userId") String userId);
    

    @Query("SELECT COUNT(u) FROM UserInfoEntity u")
    long countUsers();

    List<UserInfoEntity> findByRoleCd(String roleCd);

    @Query("SELECT u FROM UserInfoEntity u " +
           "WHERE u.roleCd = :role " +
           "AND (LOWER(u.userId) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "  OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<UserInfoEntity> searchByRoleAndKeyword(String role, String q);
}

