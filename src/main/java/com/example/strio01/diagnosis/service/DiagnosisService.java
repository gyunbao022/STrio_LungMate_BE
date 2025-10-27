package com.example.strio01.diagnosis.service;

import java.util.List;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;

public interface DiagnosisService {
    long countProcess();
    List<DiagnosisDTO> listProcess(PageDTO pv);
    void insertProcess(DiagnosisDTO dto);
    DiagnosisDTO contentProcess(long diagId);
    void updateProcess(DiagnosisDTO dto);
    void deleteProcess(long diagId);
    List<DiagnosisDTO> findByXrayId(long xrayId);
}
