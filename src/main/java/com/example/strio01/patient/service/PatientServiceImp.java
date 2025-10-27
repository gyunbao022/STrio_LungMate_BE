package com.example.strio01.patient.service;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.patient.dto.PatientDTO;
import com.example.strio01.patient.entity.PatientEntity;
import com.example.strio01.patient.repository.PatientRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PatientServiceImp implements PatientService {
	@Autowired
	private PatientRepository patientRepository;

	public PatientServiceImp() {

	}

	@Transactional
	@Override
	public long countProcess() {
		long cnt = patientRepository.count();
		return cnt;
	}

	
	@Transactional
	@Override
	public PatientDTO contentProcess(long patientId) {

		PatientEntity patientEntity = patientRepository.findPatientByPatientId(patientId);
		return PatientDTO.toDTO(patientEntity);
	}



}





