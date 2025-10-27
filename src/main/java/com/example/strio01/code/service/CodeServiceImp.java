package com.example.strio01.code.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.example.strio01.code.dto.CodeDTO;
import com.example.strio01.code.entity.CodeEntity;
import com.example.strio01.code.repository.CodeRepository;

//@Slf4j
@Service
public class CodeServiceImp implements CodeService {

    @Autowired
    private CodeRepository codeRepository;

    @Transactional
    @Override
    public CodeDTO getCode(String codeGroup, String codeId) {
        CodeEntity entity = codeRepository.findCode(codeGroup, codeId);
        if (entity == null) {
            //log.warn("코드 없음: group={}, id={}", codeGroup, codeId);
            return null;
        }
        return CodeDTO.toDTO(entity);
    }
}
