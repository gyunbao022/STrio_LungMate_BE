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

    // ✅ 프론트에서 필요로 하는 이미지 URL 필드 추가
    private String originalUrl;   // 원본 X-ray 이미지 경로
    private String overlayUrl;    // Grad-CAM 오버레이 이미지 경로

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
                // ✅ Entity에는 저장 안 해도 됨 (DB에 칼럼이 없을 수 있음)
                .build();
    }

    // Entity → DTO
    public static DiagnosisDTO toDTO(DiagnosisEntity entity) {
        DiagnosisDTO dto = DiagnosisDTO.builder()
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

        // ✅ 여기서 실제 URL을 설정 (컨트롤러/서비스에서 set 가능)
        // 예: dto.setOriginalUrl("/images/xray/xxx.png");
        //     dto.setOverlayUrl("/images/cam/xxx_cam.png");
        return dto;
    }
}
