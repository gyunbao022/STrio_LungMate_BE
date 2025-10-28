package com.example.strio01.uploadhistory.repository;

import com.example.strio01.xray.entity.XrayImageEntity;
import com.example.strio01.xray.repository.XrayImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 업로드 내역 전용 Repository (Facade 패턴)
 * 내부적으로 XrayImageRepository를 사용
 */
@Repository
public class UploadHistoryRepository {
    
    @Autowired
    private XrayImageRepository xrayImageRepository;  // ⬅️ XrayImageRepository를 주입받음
    
    public List<XrayImageEntity> findByUploaderId(String uploaderId) {
        return xrayImageRepository.findByUploaderIdOrderByUploadDateDesc(uploaderId);
    }
    
    public List<XrayImageEntity> findAllHistory() {
        return xrayImageRepository.findAllByOrderByUploadDateDesc();
    }
    
    public Optional<XrayImageEntity> findById(Long id) {
        return xrayImageRepository.findById(id);
    }
    
    public void delete(XrayImageEntity entity) {
        xrayImageRepository.delete(entity);
    }
    
    public List<XrayImageEntity> findByStatus(String status) {
        return xrayImageRepository.findByStatusOrderByUploadDateDesc(status);
    }
    
    public List<XrayImageEntity> findByUploaderIdAndStatus(String uploaderId, String status) {
        return xrayImageRepository.findByUploaderIdAndStatusOrderByUploadDateDesc(uploaderId, status);
    }
    
    public List<XrayImageEntity> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return xrayImageRepository.findByUploadDateBetweenOrderByUploadDateDesc(startDate, endDate);
    }
    
    public List<XrayImageEntity> findByPatientId(String patientId) {
        return xrayImageRepository.findByPatientIdOrderByUploadDateDesc(patientId);
    }
    
    public List<XrayImageEntity> findByDoctorId(String doctorId) {
        return xrayImageRepository.findByDoctorIdOrderByUploadDateDesc(doctorId);
    }
    
    public long countByStatus(String status) {
        return xrayImageRepository.countByStatus(status);
    }
    
    public long countByUploaderIdAndStatus(String uploaderId, String status) {
        return xrayImageRepository.countByUploaderIdAndStatus(uploaderId, status);
    }
}