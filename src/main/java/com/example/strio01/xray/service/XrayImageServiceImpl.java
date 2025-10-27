package com.example.strio01.xray.service;

import java.io.File;
import java.sql.Date;
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
        long newId = repository.getNextVal();
        dto.setXrayId(newId);
        dto.setCreatedAt(new Date(System.currentTimeMillis()));

        // 파일 업로드 처리
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String filename = newId + "_" + dto.getFile().getOriginalFilename();
                File dest = new File(tempDir, filename);
                dto.getFile().transferTo(dest);
                dto.setFilePath(dest.getAbsolutePath());
            } catch (Exception e) {
                log.error("File upload failed", e);
            }
        }

        repository.save(dto.toEntity());
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
        dto.setUpdatedAt(new Date(System.currentTimeMillis()));
        repository.updateStatus(dto.toEntity());
    }

    @Transactional
    @Override
    public void deleteProcess(long xrayId, String tempDir) {
        XrayImageEntity entity = repository.findByXrayId(xrayId);
        if (entity != null && entity.getFilePath() != null) {
            File file = new File(entity.getFilePath());
            if (file.exists()) file.delete();
        }
        repository.deleteById(xrayId);
    }
}
