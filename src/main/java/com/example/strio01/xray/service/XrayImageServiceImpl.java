package com.example.strio01.xray.service;

import java.io.File;
import java.time.LocalDateTime;  // ⬅️ Date 대신 LocalDateTime
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.xray.dto.XrayImageDTO;
import com.example.strio01.xray.entity.XrayImageEntity;
import com.example.strio01.xray.repository.XrayImageRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XrayImageServiceImpl implements XrayImageService {

    @Autowired
    private XrayImageRepository repository;

    @Transactional
    @Override
    public long countProcess() {
        return repository.count();
    }

    @Transactional
    @Override
    public List<XrayImageDTO> listProcess(PageDTO pv) {
        return repository.findPagedXrayByRownum(pv)
                .stream().map(XrayImageDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void insertProcess(XrayImageDTO dto, String tempDir) {

//        long newId = repository.getNextVal(); 2510528
//        dto.setXrayId(newId); 251028
    	
        // ⬅️ Date 대신 LocalDateTime.now() 사용
        dto.setCreatedAt(LocalDateTime.now());

        XrayImageEntity entity = dto.toEntity();
        entity.setXrayId(null);
        
        entity.setFilePath("temp");
        entity.setFileName("temp");
        
        if (entity.getFileSize()==null) {
        	entity.setFileSize(0L);
        }
        
        XrayImageEntity savedEntity = repository.save(entity);
        
        // 파일 업로드 처리
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
//                String filename = newId + "_" + dto.getFile().getOriginalFilename();
            		  String filename = savedEntity.getXrayId() + "_" + dto.getFile().getOriginalFilename();
                File dest = new File(tempDir, filename);
                dto.getFile().transferTo(dest);
//                dto.setFilePath(dest.getAbsolutePath());
                
                // ⬅️ 파일명과 크기 저장 (업로드 내역용)
//                dto.setFileName(filename);
//                dto.setFileSize(dto.getFile().getSize());
                savedEntity.setFilePath(dest.getAbsolutePath());
                savedEntity.setFileName(filename);
                savedEntity.setFileSize(dto.getFile().getSize());
                
                repository.save(savedEntity);
            } catch (Exception e) {
                log.error("File upload failed", e);
            }
        }

//        repository.save(dto.toEntity());
    }

    @Transactional
    @Override
    public XrayImageDTO contentProcess(long xrayId) {
        XrayImageEntity entity = repository.findByXrayId(xrayId);
        return XrayImageDTO.toDTO(entity);
    }

    @Transactional
    @Override
    public void updateStatusProcess(XrayImageDTO dto) {
        // ⬅️ Date 대신 LocalDateTime.now() 사용
        dto.setUpdatedAt(LocalDateTime.now());
        repository.updateStatus(dto.toEntity());
    }

    @Transactional
    @Override
    public void deleteProcess(long xrayId, String tempDir) {
        XrayImageEntity entity = repository.findByXrayId(xrayId);
        if (entity != null && entity.getFilePath() != null) {
            File file = new File(entity.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("파일 삭제 완료: {}", entity.getFilePath());
                } else {
                    log.warn("파일 삭제 실패: {}", entity.getFilePath());
                }
            }
        }
        repository.deleteById(xrayId);
    }
}