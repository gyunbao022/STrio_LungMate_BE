package com.example.strio01.xray.dto;

import java.sql.Date;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.example.strio01.xray.entity.XrayImageEntity;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
public class XrayImageDTO {

    private Long xrayId;
    private Long patientId;
    private String doctorId;
    private String uploaderId;
    private String filePath;
    private String statusCd;
    private Date createdAt;
    private Date updatedAt;

    // 파일 업로드 관련
    private MultipartFile file; // 업로드된 파일

    // DTO → Entity
    public XrayImageEntity toEntity() {
        return XrayImageEntity.builder()
                .xrayId(xrayId)
                .patientId(patientId)
                .doctorId(doctorId)
                .uploaderId(uploaderId)
                .filePath(filePath)
                .statusCd(statusCd)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // Entity → DTO
    public static XrayImageDTO toDTO(XrayImageEntity entity) {
        return XrayImageDTO.builder()
                .xrayId(entity.getXrayId())
                .patientId(entity.getPatientId())
                .doctorId(entity.getDoctorId())
                .uploaderId(entity.getUploaderId())
                .filePath(entity.getFilePath())
                .statusCd(entity.getStatusCd())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
