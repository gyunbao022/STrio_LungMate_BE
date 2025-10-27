package com.example.strio01.diagnosis.entity;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "DIAGNOSIS")
public class DiagnosisEntity {

    @Id
    @Column(name = "DIAG_ID")
    private Long diagId;

    @Column(name = "XRAY_ID", nullable = false)
    private Long xrayId;

    @Column(name = "DOCTOR_ID", nullable = false, length = 50)
    private String doctorId;

    @Column(name = "AI_RESULT", length = 200)
    private String aiResult;

    @Column(name = "AI_IMPRESSION", length = 4000)
    private String aiImpression;

    @Column(name = "DOCTOR_RESULT", nullable = false, length = 200)
    private String doctorResult;

    @Column(name = "DOCTOR_IMPRESSION", length = 4000)
    private String doctorImpression;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;
}
