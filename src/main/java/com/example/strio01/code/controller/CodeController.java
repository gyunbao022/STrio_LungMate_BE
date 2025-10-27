package com.example.strio01.code.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.strio01.code.dto.CodeDTO;
import com.example.strio01.code.service.CodeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/code")
public class CodeController {

    @Autowired
    private CodeService codeService;

    // 예: /code/view/USER_ROLE/ADMIN
    @GetMapping("/view/{group}/{id}")
    public ResponseEntity<CodeDTO> viewCode(
            @PathVariable("group") String group,
            @PathVariable("id") String id) {

        CodeDTO dto = codeService.getCode(group, id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}
