package com.example.strio01.uploadhistory.service;

import com.example.strio01.uploadhistory.dto.UploadHistoryDTO;
import com.example.strio01.uploadhistory.repository.UploadHistoryRepository;
import com.example.strio01.xray.entity.XrayImageEntity;
import com.example.strio01.user.repository.UserInfoRepository;
import com.example.strio01.user.entity.UserInfoEntity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UploadHistoryServiceImpl implements UploadHistoryService {
    
    @Autowired
    private UploadHistoryRepository uploadHistoryRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;
    
    @Override
    @Transactional(readOnly = true)
    public List<UploadHistoryDTO> getMyHistory(String uploaderId) {
        log.info("===> [Service] 본인 업로드 내역 조회: uploaderId={}", uploaderId);
        
        List<XrayImageEntity> entities = uploadHistoryRepository.findByUploaderId(uploaderId);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UploadHistoryDTO> getAllHistory() {
        log.info("===> [Service] 전체 업로드 내역 조회");
        
        List<XrayImageEntity> entities = uploadHistoryRepository.findAllHistory();
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteXray(Long xrayId, String userId, String userRole) {
        log.info("===> [Service] X-ray 삭제: xrayId={}, userId={}, role={}", 
                xrayId, userId, userRole);
        
        XrayImageEntity entity = uploadHistoryRepository.findById(xrayId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "X-ray를 찾을 수 없습니다."));
        
        // ADMIN이 아니면 본인 업로드만 삭제 가능
        if (!"A".equals(userRole) && !userId.equals(entity.getUploaderId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 업로드한 항목만 삭제할 수 있습니다.");
        }
        
        // 파일 삭제
        deleteFile(entity.getFileName());
        
        // DB 삭제
        uploadHistoryRepository.delete(entity);
        
        log.info("===> [Service] X-ray 삭제 완료: xrayId={}", xrayId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UploadHistoryDTO getHistoryDetail(Long xrayId) {
        XrayImageEntity entity = uploadHistoryRepository.findById(xrayId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "X-ray를 찾을 수 없습니다."));
        
        return convertToDTO(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UploadHistoryDTO> getHistoryByStatus(String status) {
        log.info("===> [Service] 상태별 업로드 내역 조회: status={}", status);
        
        List<XrayImageEntity> entities = uploadHistoryRepository.findByStatus(status);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UploadHistoryDTO> getMyHistoryByStatus(String uploaderId, String status) {
        log.info("===> [Service] 본인 업로드 내역 조회 (상태별): uploaderId={}, status={}", 
                uploaderId, status);
        
        List<XrayImageEntity> entities = uploadHistoryRepository
                .findByUploaderIdAndStatus(uploaderId, status);
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private UploadHistoryDTO convertToDTO(XrayImageEntity entity) {
        // 업로더 이름 조회
        String uploaderName = getUserName(entity.getUploaderId());
        // 의사 이름 조회
        String doctorName = getUserName(entity.getDoctorId());
        
        return UploadHistoryDTO.builder()
                .xrayId(entity.getXrayId() != null ? entity.getXrayId().toString() : "")
                .patientId(entity.getPatientId())
                .uploaderId(entity.getUploaderId())
                .uploaderName(uploaderName != null ? uploaderName : entity.getUploaderId())
                .doctorId(entity.getDoctorId())
                .doctorName(doctorName != null ? doctorName : entity.getDoctorId())
                .uploadDate(entity.getUploadDate())
                .registrationDate(entity.getUploadDate())
                .status(entity.getStatus() != null ? entity.getStatus() : "PENDING")
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .build();
    }
    
    /**
     * 사용자 이름 조회
     */
    private String getUserName(String userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return userInfoRepository.findById(userId)
                    .map(UserInfoEntity::getUserName)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("===> 사용자 이름 조회 실패: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 파일 삭제
     */
    private void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("===> 파일 삭제 완료: {}", fileName);
            } else {
                log.warn("===> 파일이 존재하지 않음: {}", fileName);
            }
        } catch (IOException e) {
            log.error("===> 파일 삭제 실패: {}", fileName, e);
            // 파일 삭제 실패해도 DB는 삭제되도록 예외를 던지지 않음
        }
    }
}