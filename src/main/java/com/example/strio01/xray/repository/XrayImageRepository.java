package com.example.strio01.xray.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.xray.entity.XrayImageEntity;

@Repository
public interface XrayImageRepository extends JpaRepository<XrayImageEntity, Long> {

    @Query(value = """
            SELECT b.* FROM (
                SELECT rownum AS rm, a.* 
                FROM (SELECT * FROM XRAY_IMAGE ORDER BY XRAY_ID DESC) a
            ) b
            WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
            """, nativeQuery = true)
    List<XrayImageEntity> findPagedXrayByRownum(@Param("pv") PageDTO pv);

//251028
//    @Query(value = "SELECT xray_id_seq.NEXTVAL FROM dual", nativeQuery = true)
//    long getNextVal();

    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE XRAY_ID = :xrayId", nativeQuery = true)
    XrayImageEntity findByXrayId(@Param("xrayId") long xrayId);

    @Modifying
    @Query("""
            UPDATE XrayImageEntity x
            SET x.statusCd = :#{#entity.statusCd},
                x.updatedAt = :#{#entity.updatedAt}
            WHERE x.xrayId = :#{#entity.xrayId}
            """)
    void updateStatus(@Param("entity") XrayImageEntity entity);
    
 // ========== 업로드 내역 조회용 메서드 추가 ==========
    
    /**
     * 특정 업로더의 업로드 내역 조회 (최신순)
     * Oracle DB이므로 createdAt 기준으로 정렬
     */
    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE UPLOADER_ID = :uploaderId ORDER BY CREATED_AT DESC", 
           nativeQuery = true)
    List<XrayImageEntity> findByUploaderIdOrderByUploadDateDesc(@Param("uploaderId") String uploaderId);
    
    /**
     * 전체 업로드 내역 조회 (최신순)
     */
    @Query(value = "SELECT * FROM XRAY_IMAGE ORDER BY CREATED_AT DESC", 
           nativeQuery = true)
    List<XrayImageEntity> findAllByOrderByUploadDateDesc();
    
    /**
     * 상태별 조회
     */
    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE STATUS_CD = :status ORDER BY CREATED_AT DESC", 
           nativeQuery = true)
    List<XrayImageEntity> findByStatusOrderByUploadDateDesc(@Param("status") String status);
    
    /**
     * 업로더 + 상태별 조회
     */
    @Query(value = """
            SELECT * FROM XRAY_IMAGE 
            WHERE UPLOADER_ID = :uploaderId AND STATUS_CD = :status 
            ORDER BY CREATED_AT DESC
            """, nativeQuery = true)
    List<XrayImageEntity> findByUploaderIdAndStatusOrderByUploadDateDesc(
            @Param("uploaderId") String uploaderId, 
            @Param("status") String status);
    
    /**
     * 특정 기간 내 업로드 내역
     */
    @Query(value = """
            SELECT * FROM XRAY_IMAGE 
            WHERE CREATED_AT BETWEEN :startDate AND :endDate 
            ORDER BY CREATED_AT DESC
            """, nativeQuery = true)
    List<XrayImageEntity> findByUploadDateBetweenOrderByUploadDateDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 환자 ID로 조회
     */
    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE PATIENT_ID = :patientId ORDER BY CREATED_AT DESC", 
           nativeQuery = true)
    List<XrayImageEntity> findByPatientIdOrderByUploadDateDesc(@Param("patientId") String patientId);
    
    /**
     * 의사 ID로 조회
     */
    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE DOCTOR_ID = :doctorId ORDER BY CREATED_AT DESC", 
           nativeQuery = true)
    List<XrayImageEntity> findByDoctorIdOrderByUploadDateDesc(@Param("doctorId") String doctorId);
    
    /**
     * 상태별 건수
     */
    @Query(value = "SELECT COUNT(*) FROM XRAY_IMAGE WHERE STATUS_CD = :status", 
           nativeQuery = true)
    long countByStatus(@Param("status") String status);
    
    /**
     * 업로더별 상태 건수
     */
    @Query(value = "SELECT COUNT(*) FROM XRAY_IMAGE WHERE UPLOADER_ID = :uploaderId AND STATUS_CD = :status", 
           nativeQuery = true)
    long countByUploaderIdAndStatus(
            @Param("uploaderId") String uploaderId, 
            @Param("status") String status);
    
    /**
     * 사용자별 조회
     * @param uploaderId
     * @param pv
     * @return
     */
    @Query(value = """
            SELECT b.* FROM (
                SELECT rownum AS rm, a.* 
                FROM (SELECT * FROM XRAY_IMAGE WHERE UPLOADER_ID = :uploaderId ORDER BY XRAY_ID DESC) a
            ) b
            WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
            """, nativeQuery = true)
    List<XrayImageEntity> findPagedByUploaderId(@Param("uploaderId") String uploaderId,
                                                @Param("pv") PageDTO pv);

    @Query(value = "SELECT COUNT(*) FROM XRAY_IMAGE WHERE UPLOADER_ID = :uploaderId", nativeQuery = true)
    long countByUploaderId(@Param("uploaderId") String uploaderId);
    
}
