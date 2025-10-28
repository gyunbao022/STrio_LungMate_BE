package com.example.strio01.uploadhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadHistoryDTO {
    
    private String xrayId;
    private String patientId;
    private String uploaderId;
    private String uploaderName;
    private String doctorId;
    private String doctorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;
    
    private String status; // PENDING, COMPLETED, FAILED
    private String fileName;
    private Long fileSize;
}