package com.example.strio01.diagnosis.service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ spring만 사용

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import com.example.strio01.diagnosis.repository.DiagnosisRepository;
import com.example.strio01.xray.entity.XrayImageEntity;
import com.example.strio01.xray.repository.XrayImageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiagnosisServiceImp implements DiagnosisService {

    @Autowired private DiagnosisRepository repository;
    @Autowired private XrayImageRepository xrayRepo;

    @Value("${python.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    // 정적 루트/URL 프리픽스
    @Value("${strio.static-root:}")
    private String staticRoot; // 예: C:/web__ai

    @Value("${strio.images-url-prefix:}")
    private String imagesUrlPrefix; // 예: /images/

    // LLM 요약용 절대 URL 변환 베이스
    @Value("${app.public-base-url:http://localhost:8090}")
    private String publicBaseUrl;

    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    @Override
    public long countProcess() {
        return repository.count();
    }

    @Transactional
    @Override
    public List<DiagnosisDTO> listProcess(PageDTO pv) {
        return repository.findPagedDiagnosis(pv).stream().map(DiagnosisDTO::toDTO).toList();
    }

    @Transactional
    @Override
    public void insertProcess(DiagnosisDTO dto) {
        long newId = repository.getNextVal();
        dto.setDiagId(newId);
        dto.setCreatedAt(new Date(System.currentTimeMillis()));
        repository.save(dto.toEntity());
    }

    @Transactional
    @Override
    public DiagnosisDTO contentProcess(long diagId) {
        return DiagnosisDTO.toDTO(repository.findByDiagId(diagId));
    }

    // 컨트롤러가 기대하는 시그니처: 저장 후 엔티티 반환
    @Transactional
    @Override
    public DiagnosisEntity updateProcess(DiagnosisDTO dto) {
        DiagnosisEntity target;

        if (dto.getDiagId() != null) {
            target = repository.findById(dto.getDiagId())
                .orElseThrow(() -> new RuntimeException("진단 건을 찾을 수 없습니다: diagId=" + dto.getDiagId()));
        } else {
            target = repository.findLatestByXrayIdOptional(dto.getXrayId())
                .orElseThrow(() -> new RuntimeException("해당 X-ray의 최신 진단건이 없습니다: xrayId=" + dto.getXrayId()));
        }

        if (dto.getDoctorResult() != null)     target.setDoctorResult(dto.getDoctorResult());
        if (dto.getDoctorImpression() != null) target.setDoctorImpression(dto.getDoctorImpression());
        if (dto.getAiResult() != null)         target.setAiResult(dto.getAiResult());
        if (dto.getAiImpression() != null)     target.setAiImpression(dto.getAiImpression());
        if (dto.getDoctorId() != null)         target.setDoctorId(dto.getDoctorId());

        target.setUpdatedAt(new Date(System.currentTimeMillis()));
        //return repository.save(target);
        DiagnosisEntity saved = repository.save(target);
        
        // ---- Xray 상태 업데이트 ----   2025.10.30  jaemin
        try {
            if (saved.getXrayId() != null) {
                XrayImageEntity xray = xrayRepo.findById(saved.getXrayId()).orElse(null);
                if (xray != null) {
                    String result = saved.getDoctorResult();
                    // 의사 진단결과가 "PENDING"이 아니면 완료 처리
                    if (result != null && !"PENDING".equalsIgnoreCase(result)) {
                        xray.setStatusCd("D");  // 완료(Done)
                    } else {
                        xray.setStatusCd("P");  // 아직 대기(Pending)
                    }
                    xray.setUpdatedAt(java.time.LocalDateTime.now());
                    xrayRepo.save(xray);
                    log.info("XrayImage 상태 갱신: xrayId={} → statusCd={}", xray.getXrayId(), xray.getStatusCd());
                }
            }
        } catch (Exception e) {
            log.warn("XrayImage 상태 업데이트 실패: {}", e.getMessage());
        }

        return saved;        
    }

    @Transactional
    @Override
    public void deleteProcess(long diagId) {
        repository.deleteById(diagId);
    }

    @Transactional
    @Override
    public List<DiagnosisDTO> findByXrayId(long xrayId) {
        return repository.findByXrayId(xrayId).stream().map(DiagnosisDTO::toDTO).toList();
    }

    // ---------- 파이썬 호출 + CAM/원본 URL 표준화 + LLM 요약 ----------
    @Transactional
    @Override
    public Map<String, Object> analyzeByXrayId(long xrayId, Double thresholdOpt, String doctorId) {
        // 1) XRAY 파일 경로 확보(+실존 체크)
        XrayImageEntity img = xrayRepo.findById(xrayId).orElse(null);
        if (img == null || img.getFilePath() == null) {
            throw new RuntimeException("XRAY_IMAGE 경로를 찾지 못했습니다. xrayId=" + xrayId);
        }

        // 웹/상대 경로도 파일시스템 절대경로로 변환
        Path fsPath = toFsPath(img.getFileName());		// 2025.10.30 이미지 경로 변경 jaemin   getFilePath() => getFileName() 
        requireExists(fsPath, "X-ray 파일이 존재하지 않습니다");
        
        System.out.println("=======================================");
        System.out.println("=============img.getFilePath():"+img.getFilePath());
        System.out.println("=============fsPath:"+fsPath);
        
        // 2) 파이썬 analyze 호출
        String boundary = "----StrioBoundary" + System.currentTimeMillis();
        byte[] body = buildMultipartBody(boundary, fsPath, thresholdOpt, xrayId);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(java.net.URI.create(pythonBaseUrl + "/api/analyze"))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        Map<String, Object> py = doJson(client, req);

        if (!Boolean.TRUE.equals(py.get("ok"))) {
            throw new RuntimeException("파이썬 분석 실패: " + safeTrunc(py.get("error"), 300));
        }

        Double predProb = toDouble(py.get("pred_prob"));
        String predLabel = (String) py.get("pred_label");
        Double usedTh = toDouble(py.get("threshold_used"));
        String camLayer = (String) py.get("target_layer_used");

        Map<String, Object> images = asMap(py.get("images"));
        String originalUrl = images != null ? normalizeToWebUrl((String) images.get("original")) : null;
        String overlayUrl  = images != null ? normalizeToWebUrl((String) images.get("overlay"))  : null;

        // 원본 URL 보완
        if (isBlank(originalUrl) && img.getFilePath() != null) {
            originalUrl = toWebUrlFromFilePath(img.getFileName());	// 2025.10.30 이미지 경로 변경 jaemin   getFilePath() => getFileName()
        }
        // 오버레이 기본 경로
        if (isBlank(overlayUrl)) {
            overlayUrl = joinUrl(imagesUrlPrefix, "cam/" + xrayId + "_cam.png");
        }

        // 3) LLM 요약 호출용: 절대 URL
        String absoluteOriginalUrl = toAbsoluteUrl(originalUrl);
        System.out.println("========================= absoluteOriginalUrl:"+absoluteOriginalUrl);
        String aiImpression = null;
        try {
            aiImpression = callLlmSummarize(client, predLabel, predProb, absoluteOriginalUrl);
        } catch (Exception e) {
            log.warn("LLM summarize 실패(무시): {}", e.toString());
        }

        // 4) DB 저장
        long newId = repository.getNextVal();
        DiagnosisEntity toSave = DiagnosisEntity.builder()
            .diagId(newId)
            .xrayId(xrayId)
            .doctorId("SYSTEM")
            .aiResult(predLabel)
            .aiImpression(aiImpression)
            .doctorResult("PENDING")
            .doctorImpression(null)
            .createdAt(new Date(System.currentTimeMillis()))
            .updatedAt(null)
            .build();
        repository.save(toSave);

        // 5) 프론트 응답
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("diagId", newId);
        resp.put("xrayId", xrayId);
        resp.put("pred", predLabel);
        resp.put("prob", predProb);
        resp.put("overlayUrl", overlayUrl);
        resp.put("originalUrl", originalUrl);
        resp.put("camLayer", camLayer);
        resp.put("threshold", usedTh);

        log.info("🩻 originalUrl -> {}", originalUrl);
        log.info("🎨 overlayUrl  -> {}", overlayUrl);
        return resp;
    }

    @Transactional
    @Override
    public Map<String, Object> latestResultView(long xrayId) {
        DiagnosisEntity e = repository.findLatestByXrayIdOptional(xrayId).orElse(null);
        if (e == null) return Collections.emptyMap();

        XrayImageEntity img = xrayRepo.findById(xrayId).orElse(null);

        String originalUrl = null;
        if (img != null && img.getFilePath() != null) {
            //originalUrl = toWebUrlFromFilePath(img.getFilePath()); 
            originalUrl = toWebUrlFromFilePath(img.getFileName());		// 2025.10.30 xray 이미지 경로 이슈 해결. jaemin
        }
        String overlayUrl = joinUrl(imagesUrlPrefix, "cam/" + xrayId + "_cam.png");

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("xrayId", xrayId);
        resp.put("pred", e.getAiResult());
        resp.put("prob", null);
        resp.put("overlayUrl", overlayUrl);
        resp.put("originalUrl", originalUrl);
        resp.put("camLayer", null);
        resp.put("threshold", null);
        
        // 추가: 프론트에서 필요로 하는 진단 정보
        resp.put("doctorResult", e.getDoctorResult());
        resp.put("doctorImpression", e.getDoctorImpression());
        resp.put("aiImpression", e.getAiImpression());
        resp.put("statusCd", 
            (e.getDoctorResult() != null && !"PENDING".equalsIgnoreCase(e.getDoctorResult()))
            ? "D" : "P"   // doctorResult가 확정되었으면 COMPLETED 처리
        );        

        log.info("[latestResultView] originalUrl -> {}", resp.get("originalUrl"));
        log.info("[latestResultView] overlayUrl  -> {}", resp.get("overlayUrl"));
        return resp;
    }

    // ---------- Helpers ----------
    private String safeTrunc(Object o, int n) {
        String s = String.valueOf(o);
        return s.length() > n ? s.substring(0, n) + "..." : s;
    }

    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number num) return num.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?,?> m) return (Map<String, Object>) m;
        return null;
    }

    private Map<String, Object> doJson(HttpClient client, HttpRequest req) {
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300)
                throw new RuntimeException("HTTP " + res.statusCode() + " " + safeTrunc(res.body(), 300));
            return om.readValue(res.body(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("파이썬 호출 실패: " + e.getMessage(), e);
        }
    }

    private byte[] buildMultipartBody(String boundary, Path file, Double threshold, Long xrayId) {
        try {
            String sep = "--" + boundary + "\r\n";
            String end = "--" + boundary + "--\r\n";
            byte[] fileBytes = Files.readAllBytes(file);

            String contentType = guessContentType(file);
            String fileHeader =
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getFileName() + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n";

            StringBuilder sb = new StringBuilder();
            sb.append(sep).append("Content-Disposition: form-data; name=\"xrayId\"\r\n\r\n").append(xrayId).append("\r\n");
            if (threshold != null) {
                sb.append(sep).append("Content-Disposition: form-data; name=\"threshold\"\r\n\r\n").append(threshold).append("\r\n");
            }
            sb.append(sep).append(fileHeader);

            byte[] head = sb.toString().getBytes();
            byte[] tail = ("\r\n" + end).getBytes();

            byte[] all = new byte[head.length + fileBytes.length + tail.length];
            System.arraycopy(head, 0, all, 0, head.length);
            System.arraycopy(fileBytes, 0, all, head.length, fileBytes.length);
            System.arraycopy(tail, 0, all, head.length + fileBytes.length, tail.length);
            return all;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String callLlmSummarize(HttpClient client, String predLabel, Double predProb, String absoluteImageUrl) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("pred_label", predLabel);
            body.put("pred_prob", predProb);
            body.put("image_data_url", absoluteImageUrl); // 절대 URL

            String json = om.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(java.net.URI.create(pythonBaseUrl + "/api/llm-summarize"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            Map<String, Object> res = doJson(client, req);
            if (Boolean.TRUE.equals(res.get("ok"))) {
                Object s = res.get("summary");
                return s != null ? String.valueOf(s) : null;
            }
        } catch (Exception e) {
            log.warn("LLM summarize 호출 오류: {}", e.toString());
        }
        return null;
    }

    // ===== 경로/URL 유틸 =====
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** 웹 URL 또는 상대경로를 안전한 파일시스템 절대경로로 변환 */
    private Path toFsPath(String urlOrPath) {
        if (isBlank(urlOrPath)) return null;

        String p = urlOrPath.trim().replace('\\', '/'); // 통일

        // 절대경로(C:/..., D:/..., //server/...)면 그대로
        if (p.matches("^[A-Za-z]:/.*") || p.startsWith("//")) {
            return Paths.get(p).toAbsolutePath().normalize();
        }

        // http(s)://... 인 경우, imagesUrlPrefix 이후만 취함
        if (p.startsWith("http://") || p.startsWith("https://")) {
            int idx = p.indexOf(imagesUrlPrefix);
            if (idx >= 0) p = p.substring(idx); // "/images/..." 로 절삭
        }

        // "/images/..." → "images/"
        if (p.startsWith("/")) p = p.substring(1);

        // STATIC_ROOT 결합
        String root = (staticRoot == null ? "" : staticRoot).replace('\\', '/');
        if (!root.isEmpty() && !root.endsWith("/")) root += "/";
        Path fs = Paths.get(root + p).normalize();

        return fs.toAbsolutePath();
    }

    /** 존재 확인 + 명확한 메시지 */
    private void requireExists(Path path, String errPrefix) {
        if (path == null || !Files.exists(path)) {
            String msg = errPrefix + ": " + (path == null ? "(null)" : path.toString());
            log.warn("⚠ {}", msg);
            throw new RuntimeException(msg);
        }
    }

    private String normalizeToWebUrl(String urlOrPath) {
        if (isBlank(urlOrPath)) return null;
        String s = urlOrPath.trim();
        if (s.startsWith("data:") || s.startsWith("http://") || s.startsWith("https://")) return s;
        if (s.startsWith(imagesUrlPrefix)) return s;

        s = s.replace("\\", "/");

        // 파일시스템 절대경로를 웹 경로로 바꾸기
        if (!isBlank(staticRoot)) {
            String root = staticRoot.replace("\\", "/");
            if (!root.endsWith("/")) root += "/";
            if (s.startsWith(root)) {
                String rel = s.substring(root.length());
                if (!rel.startsWith("images/")) rel = "images/" + rel;
                return "/" + rel;
            }
        }

        // 그 외엔 /images/ 접두사
        if (!s.startsWith("/")) s = "/" + s;
        if (!s.startsWith(imagesUrlPrefix)) s = imagesUrlPrefix + (s.startsWith("/") ? s.substring(1) : s);
        return s;
    }

    private String toWebUrlFromFilePath(String filePath) {
        if (filePath == null) return null;
        return normalizeToWebUrl(filePath);
        }

    private String joinUrl(String prefix, String tail) {
        if (isBlank(prefix)) prefix = "/";
        if (!prefix.startsWith("/")) prefix = "/" + prefix;
        if (!prefix.endsWith("/")) prefix += "/";
        if (tail.startsWith("/")) tail = tail.substring(1);
        return prefix + tail;
    }

    // 상대(/images/**) → 절대(http://host:port/images/**)
    private String toAbsoluteUrl(String maybeRelative) {
        if (isBlank(maybeRelative)) return null;
        String s = maybeRelative.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) return s;
        String base = publicBaseUrl;
        if (isBlank(base)) base = "http://localhost:8090";
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!s.startsWith("/")) s = "/" + s;
        return base + s;
    }

    private String guessContentType(Path file) {
        try {
            String probed = Files.probeContentType(file);
            if (probed != null) return probed;
        } catch (IOException ignore) {}
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".bmp")) return "image/bmp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
