package com.example.strio01.diagnosis.dto;

import java.sql.Date;
import org.springframework.stereotype.Component;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
public class DiagnosisDTO {

    private Long diagId;
    private Long xrayId;
    private String doctorId;
    private String aiResult;
    private String aiImpression;
    private String doctorResult;
    private String doctorImpression;
    private Date createdAt;
    private Date updatedAt;

    // DTO → Entity
    public DiagnosisEntity toEntity() {
        return DiagnosisEntity.builder()
                .diagId(diagId)
                .xrayId(xrayId)
                .doctorId(doctorId)
                .aiResult(aiResult)
                .aiImpression(aiImpression)
                .doctorResult(doctorResult)
                .doctorImpression(doctorImpression)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // Entity → DTO
    public static DiagnosisDTO toDTO(DiagnosisEntity entity) {
        return DiagnosisDTO.builder()
                .diagId(entity.getDiagId())
                .xrayId(entity.getXrayId())
                .doctorId(entity.getDoctorId())
                .aiResult(entity.getAiResult())
                .aiImpression(entity.getAiImpression())
                .doctorResult(entity.getDoctorResult())
                .doctorImpression(entity.getDoctorImpression())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
