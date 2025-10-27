package com.example.strio01.patient.controller;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.patient.dto.PatientDTO;
import com.example.strio01.patient.repository.PatientRepository;
import com.example.strio01.patient.service.PatientService;
import com.example.strio01.common.file.FileUpload;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.proxy.annotation.GetProxy;


//@CrossOrigin("*")

@Slf4j
@RestController
public class PatientController {

    private final PatientRepository patientRepository;
    @Autowired
	private PatientService patientService;
   
    
    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;

    }
    
    
    
    @GetMapping(value="/patient/view/{num}")
    public ResponseEntity<PatientDTO> viewExecute(@PathVariable("num") Long num){
    	PatientDTO patientDTO = patientService.contentProcess(num);
    	return ResponseEntity.ok(patientDTO);
    }
  
  

}//end class











