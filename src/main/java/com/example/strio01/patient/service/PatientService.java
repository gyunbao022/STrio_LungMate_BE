package com.example.strio01.patient.service;

import java.util.List;

import com.example.strio01.patient.dto.PatientDTO;


public interface PatientService {
	public long countProcess(); 
	public PatientDTO contentProcess(long patientId);

}
