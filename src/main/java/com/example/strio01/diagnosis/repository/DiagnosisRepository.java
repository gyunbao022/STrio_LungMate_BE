package com.example.strio01.diagnosis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;

@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {

    // 페이징 조회 (Oracle rownum 방식)
    @Query(value = """
            SELECT b.* FROM (
                SELECT rownum AS rm, a.* 
                FROM (SELECT * FROM DIAGNOSIS ORDER BY DIAG_ID DESC) a
            ) b
            WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
            """, nativeQuery = true)
    List<DiagnosisEntity> findPagedDiagnosis(@Param("pv") PageDTO pv);

    // 시퀀스 생성 (별도 시퀀스명 필요 시 변경)
    @Query(value = "SELECT DIAG_ID_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    long getNextVal();

    // 단건 조회
    @Query(value = "SELECT * FROM DIAGNOSIS WHERE DIAG_ID = :id", nativeQuery = true)
    DiagnosisEntity findByDiagId(@Param("id") long id);

    // X-ray ID별 조회 (한 이미지에 여러 진단 가능)
    @Query(value = "SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC", nativeQuery = true)
    List<DiagnosisEntity> findByXrayId(@Param("xrayId") long xrayId);

    // 의사별 진단 리스트
    @Query(value = "SELECT * FROM DIAGNOSIS WHERE DOCTOR_ID = :doctorId ORDER BY CREATED_AT DESC", nativeQuery = true)
    List<DiagnosisEntity> findByDoctorId(@Param("doctorId") String doctorId);

    // 수정 (AI 결과 또는 의사 소견 수정 시)
    @Modifying
    @Query("""
            UPDATE DiagnosisEntity d
            SET d.doctorResult = :#{#entity.doctorResult},
                d.doctorImpression = :#{#entity.doctorImpression},
                d.updatedAt = :#{#entity.updatedAt}
            WHERE d.diagId = :#{#entity.diagId}
            """)
    void updateDiagnosis(@Param("entity") DiagnosisEntity entity);
}
