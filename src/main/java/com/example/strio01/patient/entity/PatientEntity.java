package com.example.strio01.patient.entity;

import java.sql.Date;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@Table(name = "patient")
public class PatientEntity {
	@Id
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_seq_generator")
	//@SequenceGenerator(name = "board_seq_generator", sequenceName = "board_num_seq", allocationSize = 1)
	private Long patientId;
	private String  patientName, birthDate, gender;
	private Date createdAt, updatedAt;	
}
