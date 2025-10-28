package com.example.strio01.uploadhistory.service;

import com.example.strio01.uploadhistory.dto.UploadHistoryDTO;
import java.util.List;

public interface UploadHistoryService {
    
    /**
     * 특정 사용자의 업로드 내역 조회
     */
    List<UploadHistoryDTO> getMyHistory(String uploaderId);
    
    /**
     * 전체 업로드 내역 조회 (관리자용)
     */
    List<UploadHistoryDTO> getAllHistory();
    
    /**
     * X-ray 삭제 (권한 체크 포함)
     */
    void deleteXray(Long xrayId, String userId, String userRole);
    
    /**
     * 특정 X-ray 상세 정보 조회
     */
    UploadHistoryDTO getHistoryDetail(Long xrayId);
    
    /**
     * 상태별 조회
     */
    List<UploadHistoryDTO> getHistoryByStatus(String status);
    
    /**
     * 업로더 + 상태별 조회
     */
    List<UploadHistoryDTO> getMyHistoryByStatus(String uploaderId, String status);
}