package com.example.strio01.diagnosis.service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import com.example.strio01.diagnosis.repository.DiagnosisRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiagnosisServiceImp implements DiagnosisService {

    @Autowired
    private DiagnosisRepository repository;

    @Transactional
    @Override
    public long countProcess() {
        return repository.count();
    }

    @Transactional
    @Override
    public List<DiagnosisDTO> listProcess(PageDTO pv) {
        return repository.findPagedDiagnosis(pv)
                .stream().map(DiagnosisDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void insertProcess(DiagnosisDTO dto) {
        long newId = repository.getNextVal();
        dto.setDiagId(newId);
        dto.setCreatedAt(new Date(System.currentTimeMillis()));
        DiagnosisEntity entity = dto.toEntity();
        repository.save(entity);
    }

    @Transactional
    @Override
    public DiagnosisDTO contentProcess(long diagId) {
        DiagnosisEntity entity = repository.findByDiagId(diagId);
        return DiagnosisDTO.toDTO(entity);
    }

    @Transactional
    @Override
    public void updateProcess(DiagnosisDTO dto) {
        dto.setUpdatedAt(new Date(System.currentTimeMillis()));
        repository.updateDiagnosis(dto.toEntity());
    }

    @Transactional
    @Override
    public void deleteProcess(long diagId) {
        repository.deleteById(diagId);
    }

    @Transactional
    @Override
    public List<DiagnosisDTO> findByXrayId(long xrayId) {
        return repository.findByXrayId(xrayId)
                .stream().map(DiagnosisDTO::toDTO)
                .collect(Collectors.toList());
    }
}
