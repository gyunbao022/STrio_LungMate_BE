package com.example.strio01.xray.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xray_seq")
    @SequenceGenerator(name = "xray_seq", sequenceName = "xray_id_seq", allocationSize = 1)
    @Column(name = "XRAY_ID")
    private Long xrayId;
    
    @Column(name = "PATIENT_ID")
    private String patientId;
    
    @Column(name = "DOCTOR_ID")
    private String doctorId;
    
    @Column(name = "UPLOADER_ID")
    private String uploaderId;
    
    @Column(name = "FILE_PATH")
    private String filePath;
    
    @Column(name = "FILE_NAME")
    private String fileName;
    
    @Column(name = "FILE_SIZE")
    private Long fileSize;
    
    @Column(name = "STATUS_CD")
    private String statusCd;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    
    // 호환성 메서드
    public LocalDateTime getUploadDate() { 
        return createdAt; 
    }
    
    public String getStatus() { 
        return statusCd; 
    }
    
    public void setStatus(String s) { 
        statusCd = s; 
    }
    
}