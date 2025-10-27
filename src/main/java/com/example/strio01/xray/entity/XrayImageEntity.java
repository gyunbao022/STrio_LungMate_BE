package com.example.strio01.xray.entity;

import java.sql.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@Table(name = "XRAY_IMAGE")
public class XrayImageEntity {

    @Id
    private Long xrayId;            // PK
    private Long patientId;         // 환자 ID
    private String doctorId;        // 담당의사 ID
    private String uploaderId;      // 업로더 ID
    private String filePath;        // 파일 저장 경로
    private String statusCd;        // 상태코드 (P/D)
    private Date createdAt;         // 생성일자
    private Date updatedAt;         // 수정일자
}
