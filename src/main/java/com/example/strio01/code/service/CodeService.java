package com.example.strio01.code.service;

import com.example.strio01.code.dto.CodeDTO;

public interface CodeService {
    CodeDTO getCode(String codeGroup, String codeId);
}
