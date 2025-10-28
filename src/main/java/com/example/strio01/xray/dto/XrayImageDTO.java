package com.example.strio01.xray.dto;

import java.time.LocalDateTime;  // ⬅️ Date 대신 LocalDateTime
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
    private String fileName;        // ⬅️ 추가
    private Long fileSize;          // ⬅️ 추가
    private String statusCd;
    private LocalDateTime createdAt;  // ⬅️ Date → LocalDateTime
    private LocalDateTime updatedAt;  // ⬅️ Date → LocalDateTime

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
                .fileName(fileName)          // ⬅️ 추가
                .fileSize(fileSize)          // ⬅️ 추가
                .statusCd(statusCd)
                .createdAt(createdAt)        // ⬅️ 이제 LocalDateTime
                .updatedAt(updatedAt)        // ⬅️ 이제 LocalDateTime
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
                .fileName(entity.getFileName())      // ⬅️ 추가
                .fileSize(entity.getFileSize())      // ⬅️ 추가
                .statusCd(entity.getStatusCd())
                .createdAt(entity.getCreatedAt())    // ⬅️ 이제 LocalDateTime
                .updatedAt(entity.getUpdatedAt())    // ⬅️ 이제 LocalDateTime
                .build();
    }
}