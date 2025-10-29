package com.example.strio01.diagnosis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import com.example.strio01.diagnosis.service.DiagnosisService;
import com.example.strio01.xray.repository.XrayImageRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class DiagnosisController {

    @Autowired
    private DiagnosisService service;

    @Autowired
    private XrayImageRepository xrayImageRepository;

    @Value("${strio.static-root}")
    private String STATIC_ROOT;

    @Value("${strio.images-url-prefix:/images/}")
    private String URL_PREFIX;

    // ------------------- 리스트 -------------------
    @GetMapping("/diagnosis/list/{page}")
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

    @GetMapping("/diagnosis/view/{id}")
    public ResponseEntity<DiagnosisDTO> view(@PathVariable("id") long id) {
        DiagnosisDTO dto = service.contentProcess(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/diagnosis/xray/{xrayId}")
    public ResponseEntity<List<DiagnosisDTO>> findByXray(@PathVariable("xrayId") long xrayId) {
        return ResponseEntity.ok(service.findByXrayId(xrayId));
    }

    // ------------------- 작성/수정/삭제 -------------------
    @PostMapping(
        value = "/diagnosis/write",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> write(@RequestBody DiagnosisDTO dto) {
        service.insertProcess(dto);
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok(body);
    }

    @PutMapping(
        value = "/diagnosis/update",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> update(@RequestBody DiagnosisDTO dto) {
        // 서비스에서 대상 행을 찾아 필드를 갱신하고 저장(업데이트)한 후 엔티티를 반환해야 합니다.
        DiagnosisEntity saved = service.updateProcess(dto);

        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("diagId", saved.getDiagId());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/diagnosis/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") long id) {
        service.deleteProcess(id);
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok(body);
    }

    // ------------------- 분석 API (인증 없이 접근 가능) -------------------
    @PostMapping(
        value = "/api/analyze/by-id",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> analyzeById(@RequestBody AnalyzeReq req) {
        Map<String, Object> error = new HashMap<>();
        if (req == null || req.getXrayId() == null) {
            error.put("error", "xrayId required");
            return ResponseEntity.badRequest().body(error);
        }

        log.info("📡 Received analyze request for XrayId={}", req.getXrayId());

        Map<String, Object> resp = service.analyzeByXrayId(req.getXrayId(), req.getThreshold());
        if (resp == null || resp.isEmpty()) {
            error.put("error", "Python model server no response");
            return ResponseEntity.internalServerError().body(error);
        }

        return ResponseEntity.ok(resp);
    }

    @GetMapping(
        value = "/api/analyze/result",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> result(@RequestParam("xrayId") long xrayId) {
        Map<String, Object> resp = service.latestResultView(xrayId);
        if (resp == null || resp.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Result not found for XrayId=" + xrayId);
            return ResponseEntity.status(404).body(error);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping(
        value = "/api/diagnoses/{xrayId}/analyze",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> analyzeAlt(
            @PathVariable long xrayId,
            @RequestParam(value = "threshold", required = false) Double th) {
        Map<String, Object> resp = service.analyzeByXrayId(xrayId, th);
        return ResponseEntity.ok(resp);
    }

    // 내부 요청용 DTO
    public static class AnalyzeReq {
        private Long xrayId;
        private Double threshold;

        public Long getXrayId() { return xrayId; }
        public void setXrayId(Long xrayId) { this.xrayId = xrayId; }
        public Double getThreshold() { return threshold; }
        public void setThreshold(Double threshold) { this.threshold = threshold; }
    }
}
