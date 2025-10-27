package com.example.strio01.user.dto;

import java.sql.Timestamp;
import org.springframework.stereotype.Component;
import com.example.strio01.user.entity.UserInfoEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class UserInfoDTO {

    private String userId;
    private String passwd;
    private String newPasswd;
    private String userName;
    private String email;
    private String roleCd;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Entity → DTO
    public static UserInfoDTO toDTO(UserInfoEntity e) {
        return UserInfoDTO.builder()
                .userId(e.getUserId())
                .passwd(e.getPasswd())
                .userName(e.getUserName())
                .email(e.getEmail())
                .roleCd(e.getRoleCd())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // DTO → Entity
    public UserInfoEntity toEntity() {
        return UserInfoEntity.builder()
                .userId(userId)
                .passwd(passwd)
                .userName(userName)
                .email(email)
                .roleCd(roleCd)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
