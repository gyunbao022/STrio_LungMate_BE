package com.example.strio01.xray.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.xray.dto.XrayImageDTO;
import com.example.strio01.xray.service.XrayImageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/xray")
public class XrayImageController {

    @Autowired
    private XrayImageService service;

    @Value("${spring.servlet.multipart.location}")
    private String tempDir;

    private PageDTO pdto;

    @GetMapping("/list/{page}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable("page") int page) {
        long total = service.countProcess();
        Map<String, Object> map = new HashMap<>();
        if (total > 0) {
            pdto = new PageDTO(page, total);
            map.put("xrayList", service.listProcess(pdto));
            map.put("pv", pdto);
        }
        return ResponseEntity.ok(map);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY')")
    public ResponseEntity<String> upload(XrayImageDTO dto) {
        service.insertProcess(dto, tempDir);
        return ResponseEntity.ok("1");
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<XrayImageDTO> view(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.contentProcess(id));
    }

    @PutMapping("/updateStatus")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<Void> updateStatus(@RequestBody XrayImageDTO dto) {
        service.updateStatusProcess(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteProcess(id, tempDir);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable("filename") String filename) throws IOException {
        String fileName = filename.substring(filename.indexOf("_") + 1);
        String encoded = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

        Path path = Paths.get(tempDir + "\\" + filename);
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encoded + ";");

        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
