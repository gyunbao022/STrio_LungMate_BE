package com.example.strio01.diagnosis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;

@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {

    // 기존 페이징 (Oracle ROWNUM)
    @Query(value = """
        SELECT b.* FROM (
          SELECT rownum AS rm, a.*
          FROM (SELECT * FROM DIAGNOSIS ORDER BY DIAG_ID DESC) a
        ) b
        WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
        """, nativeQuery = true)
    List<DiagnosisEntity> findPagedDiagnosis(@Param("pv") PageDTO pv);

    @Query(value = "SELECT DIAG_ID_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    long getNextVal();

    @Query(value = "SELECT * FROM DIAGNOSIS WHERE DIAG_ID = :id", nativeQuery = true)
    DiagnosisEntity findByDiagId(@Param("id") long id);

    @Query(value = "SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC", nativeQuery = true)
    List<DiagnosisEntity> findByXrayId(@Param("xrayId") long xrayId);

    // ✅ 최신 1건 (Optional 로 안전하게)
    @Query(value = """
        SELECT * FROM (
          SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC
        ) WHERE ROWNUM = 1
        """, nativeQuery = true)
    Optional<DiagnosisEntity> findLatestByXrayIdOptional(@Param("xrayId") long xrayId);

    // ✅ 파생 메서드(참고): JPA가 만들어주는 최신순 리스트
    List<DiagnosisEntity> findByXrayIdOrderByDiagIdDesc(Long xrayId);

    // =====================================================================================
    // ✅ 추천: XRAY_IMAGE 와 조인해서 FILE_PATH 를 함께 가져오는 프로젝션
    //    - SERVICE 층에서 이 값을 이용해 originalUrl을 만들기 좋습니다.
    //    - overlayUrl 은 규칙(/images/cam/{xrayId}_cam.png)로 생성 권장.
    // =====================================================================================
    @Query(value = """
        SELECT
          d.DIAG_ID        AS diagId,
          d.XRAY_ID        AS xrayId,
          d.AI_RESULT      AS aiResult,
          d.AI_IMPRESSION  AS aiImpression,
          d.DOCTOR_RESULT  AS doctorResult,
          d.DOCTOR_IMPRESSION AS doctorImpression,
          d.CREATED_AT     AS createdAt,
          d.UPDATED_AT     AS updatedAt,
          xi.FILE_PATH     AS originalPath
        FROM (
          SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC
        ) d
        JOIN XRAY_IMAGE xi ON xi.XRAY_ID = d.XRAY_ID
        WHERE ROWNUM = 1
        """, nativeQuery = true)
    Optional<LatestDiagnosisWithImageRow> findLatestWithImageByXrayId(@Param("xrayId") long xrayId);

    // =====================================================================================
    // ✅ 프로젝션 인터페이스: 네이티브 쿼리의 별칭과 메서드명이 매핑됩니다.
    // =====================================================================================
    interface LatestDiagnosisWithImageRow {
        Long getDiagId();
        Long getXrayId();
        String getAiResult();
        String getAiImpression();
        String getDoctorResult();
        String getDoctorImpression();
        java.sql.Date getCreatedAt();
        java.sql.Date getUpdatedAt();

        // XRAY_IMAGE.FILE_PATH
        String getOriginalPath();
    }
}
