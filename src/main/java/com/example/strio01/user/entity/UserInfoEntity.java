package com.example.strio01.user.entity;

import java.sql.Timestamp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_INFO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserInfoEntity {

    @Id
    @Column(name = "USER_ID", length = 50)
    private String userId;

    @Column(name = "PASSWD", nullable = false, length = 200)
    private String passwd;

    @Column(name = "USER_NAME", nullable = false, length = 100)
    private String userName;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "ROLE_CD", nullable = false, length = 20)
    private String roleCd;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;
}
