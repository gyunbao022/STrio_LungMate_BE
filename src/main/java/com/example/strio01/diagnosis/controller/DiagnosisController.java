package com.example.strio01.diagnosis.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    @Autowired
    private DiagnosisService service;

    // 전체 리스트 (페이징)
    @GetMapping("/list/{page}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable("page") int page) {
        long total = service.countProcess();
        Map<String, Object> map = new HashMap<>();
        if (total > 0) {
            PageDTO pv = new PageDTO(page, total);
            map.put("diagnosisList", service.listProcess(pv));
            map.put("pv", pv);
        }
        return ResponseEntity.ok(map);
    }

    // 단건 조회
    @GetMapping("/view/{id}")
    public ResponseEntity<DiagnosisDTO> view(@PathVariable("id") long id) {
        DiagnosisDTO dto = service.contentProcess(id);
        return ResponseEntity.ok(dto);
    }

    // X-ray별 진단 조회
    @GetMapping("/xray/{xrayId}")
    public ResponseEntity<List<DiagnosisDTO>> findByXray(@PathVariable("xrayId") long xrayId) {
        return ResponseEntity.ok(service.findByXrayId(xrayId));
    }

    // 진단 등록 (의사 입력)
    @PostMapping("/write")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<String> write(@RequestBody DiagnosisDTO dto) {
        service.insertProcess(dto);
        return ResponseEntity.ok("1");
    }

    // 진단 수정 (의사 소견 수정)
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Void> update(@RequestBody DiagnosisDTO dto) {
        service.updateProcess(dto);
        return ResponseEntity.ok().build();
    }

    // 진단 삭제 (관리자 전용)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        service.deleteProcess(id);
        return ResponseEntity.ok().build();
    }
}
