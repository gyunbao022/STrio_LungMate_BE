package com.example.strio01.notice.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.notice.dto.NoticeDTO;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.notice.repository.NoticeRepository;
import com.example.strio01.notice.service.NoticeService;
import com.example.strio01.common.file.FileUpload;
import com.example.strio01.config.auth.PrincipalDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.proxy.annotation.GetProxy;


@CrossOrigin("*")

@Slf4j
@RestController
public class NoticeController {

    private final NoticeRepository noticeRepository;
    @Autowired
	private NoticeService noticeService;
    
    private int currentPage;
    private PageDTO pdto;
    
    @Value("${spring.servlet.multipart.location}")
    private String tempDir;
    
    
    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;

    }
    
    // http://localhost:8090/notice/list/1
    @GetMapping(value="/notice/list/{currentPage}")
    public ResponseEntity<Map<String, Object>> listExecute(@PathVariable("currentPage") int currentPage){
        Map<String, Object> map = new HashMap<>();
        
    	long totalRecord = noticeService.countProcess();
    	log.info("totalRecord: {}", totalRecord);
    	log.info("tempDir: => {  }", tempDir);
    	
    	if(totalRecord >=1) {
    		this.currentPage = currentPage;
    		this.pdto = new PageDTO(this.currentPage, totalRecord);
    		
    		map.put("noticeList", noticeService.listProcess(pdto));
    		map.put("pv", this.pdto);    		
    	}
    	return ResponseEntity.ok().body(map);
    }//end listExecute()///////////////////////////////////////////////////////////////
    
    //첨부파일이 있을 때 @RequestBody을 선언하면 안된다.
    //답변글일때 ref, reStep, reLevel 담아서 넘겨야 한다.
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/write")
    public ResponseEntity<String> writeProExecute(
            NoticeDTO dto,
            HttpServletRequest req,
            @AuthenticationPrincipal PrincipalDetails principal) {

        // 인증 사용자 ID 강제 세팅 (클라이언트 전송값 무시)
        String userId = principal.getUsername(); // 예: "admin"
        dto.setUserId(userId);

        MultipartFile file = dto.getFilename();
        log.info("file => {}", file);
        log.info("tempDir: {}", tempDir);

        if (file != null && !file.isEmpty()) {
            UUID random = FileUpload.saveCopyFile(file, tempDir);
            dto.setUpload(random + "_" + file.getOriginalFilename());
        }

        noticeService.insertProcess(dto);
        return ResponseEntity.ok(String.valueOf(1));
    }

    // 관리자만 허용
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value="/notice/update")
    public ResponseEntity<Void> updateExecute(
            NoticeDTO dto,
            HttpServletRequest req,
            @AuthenticationPrincipal PrincipalDetails principal) {

        // 인증 사용자 ID 강제 세팅
        String userId = principal.getUsername();
        dto.setUserId(userId);

        MultipartFile file = dto.getFilename();
        if (file != null && !file.isEmpty()) {
            UUID random = FileUpload.saveCopyFile(file, tempDir);
            dto.setUpload(random + "_" + file.getOriginalFilename());
        }
        noticeService.updateProcess(dto, tempDir);
        return ResponseEntity.ok(null);
    }

    // 관리자만 허용
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value="/notice/delete/{num}")
    public ResponseEntity<Void> deleteExecute(@PathVariable("num") Long num) {
        noticeService.deleteProcess(num, tempDir);
        return ResponseEntity.ok(null);
    }
    
    
}//end class











