package com.example.strio01.code.entity;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@IdClass(CodeId.class)
@Table(name = "CODE_MASTER")
public class CodeEntity {

    @Id
    @Column(name = "CODE_GROUP", length = 50)
    private String codeGroup;

    @Id
    @Column(name = "CODE_ID", length = 20)
    private String codeId;

    @Column(name = "CODE_NAME", nullable = false, length = 100)
    private String codeName;

    @Column(name = "CODE_DESC", length = 200)
    private String codeDesc;

    @Column(name = "USE_YN", length = 1)
    private String useYn;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;
}
