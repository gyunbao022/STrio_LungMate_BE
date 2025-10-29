package com.example.strio01.diagnosis.service;

import java.util.List;
import java.util.Map;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;

public interface DiagnosisService {

    long countProcess();

    List<DiagnosisDTO> listProcess(PageDTO pv);

    void insertProcess(DiagnosisDTO dto);

    DiagnosisDTO contentProcess(long diagId);

    // 컨트롤러와 일치: 저장 후 갱신된 엔티티 반환
    DiagnosisEntity updateProcess(DiagnosisDTO dto);

    void deleteProcess(long diagId);

    List<DiagnosisDTO> findByXrayId(long xrayId);

    // 스프링→파이썬→DB upsert→프론트 반환
    Map<String, Object> analyzeByXrayId(long xrayId, Double threshold);

    // 저장된 최신 결과 조회(프론트 스키마)
    Map<String, Object> latestResultView(long xrayId);
}
