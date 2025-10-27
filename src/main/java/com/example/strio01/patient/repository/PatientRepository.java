package com.example.strio01.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.patient.entity.PatientEntity;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

	//클럽 전체 리스트 보기 	
	@Query(value="SELECT b FROM PatientEntity b ")
	List<PatientEntity> findPatientList();
	
	//클럽 상세보기  	
	@Query(value="SELECT b FROM PatientEntity b where b.patientId =:patientId")
	PatientEntity findPatientByPatientId(@Param("patientId") long patientId);	
	
	
}







