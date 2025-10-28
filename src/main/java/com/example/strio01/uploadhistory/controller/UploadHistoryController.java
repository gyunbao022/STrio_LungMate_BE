package com.example.strio01.uploadhistory.controller;

import com.example.strio01.config.auth.PrincipalDetails;
import com.example.strio01.uploadhistory.dto.UploadHistoryDTO;
import com.example.strio01.uploadhistory.service.UploadHistoryService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/xray")
@CrossOrigin(origins = "*")
public class UploadHistoryController {
    
    @Autowired
    private UploadHistoryService uploadHistoryService;
    
    /**
     * 본인 업로드 내역 조회
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR','DOCTOR')")
    public ResponseEntity<List<UploadHistoryDTO>> getMyHistory(
            @AuthenticationPrincipal PrincipalDetails principal) {
        
        String userId = principal.getUsername();
        log.info("===> [본인 업로드 내역 조회] userId={}", userId);
        
        List<UploadHistoryDTO> history = uploadHistoryService.getMyHistory(userId);
        
        log.info("===> [본인 업로드 내역 조회 완료] count={}", history.size());
        return ResponseEntity.ok(history);
    }
    
    /**
     * 전체 업로드 내역 조회 (관리자용)
     */
    @GetMapping("/history/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UploadHistoryDTO>> getAllHistory() {
        log.info("===> [전체 업로드 내역 조회 요청]");
        
        List<UploadHistoryDTO> history = uploadHistoryService.getAllHistory();
        
        log.info("===> [전체 업로드 내역 조회 완료] count={}", history.size());
        return ResponseEntity.ok(history);
    }
    
    /**
     * 특정 X-ray 상세 정보 조회
     */
    @GetMapping("/history/{xrayId}")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR','DOCTOR')")
    public ResponseEntity<UploadHistoryDTO> getHistoryDetail(
            @PathVariable Long xrayId) {
        
        log.info("===> [업로드 내역 상세 조회] xrayId={}", xrayId);
        
        UploadHistoryDTO detail = uploadHistoryService.getHistoryDetail(xrayId);
        
        return ResponseEntity.ok(detail);
    }
    
    /**
     * 상태별 업로드 내역 조회
     */
    @GetMapping("/history/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR','DOCTOR')")
    public ResponseEntity<List<UploadHistoryDTO>> getHistoryByStatus(
            @PathVariable String status) {
        
        log.info("===> [상태별 업로드 내역 조회] status={}", status);
        
        List<UploadHistoryDTO> history = uploadHistoryService.getHistoryByStatus(status);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * X-ray 삭제 (권한 체크 포함)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR')")
    public ResponseEntity<Map<String, String>> deleteXray(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal PrincipalDetails principal) {
        
        String userId = principal.getUsername();
        String userRole = principal.getAuthInfo().getRoleCd();
        
        log.info("===> [X-ray 삭제 요청] xrayId={}, userId={}, role={}", id, userId, userRole);
        
        try {
            uploadHistoryService.deleteXray(id, userId, userRole);
            return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
        } catch (Exception e) {
            log.error("===> [X-ray 삭제 실패] xrayId={}", id, e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "삭제에 실패했습니다: " + e.getMessage()));
        }
    }
}