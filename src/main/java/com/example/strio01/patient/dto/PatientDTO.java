package  com.example.strio01.patient.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.patient.entity.PatientEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//import members.dto.MembersDTO;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
public class PatientDTO {
	private Long patientId;
	private String  patientName, birthDate, gender;
	private Date createdAt, updatedAt;
	
	
	// DTO -> Entity
	public PatientEntity toEntity() {
		return PatientEntity.builder()
				.patientId(patientId)
				.patientName(patientName)
				.birthDate(birthDate)
				.gender(gender)				
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
	
	
	// Entity -> DTO
	public static PatientDTO toDTO(PatientEntity patientEntity) {
		return PatientDTO.builder()
				.patientId(patientEntity.getPatientId())
				.patientName(patientEntity.getPatientName())
				.birthDate(patientEntity.getBirthDate())
				.gender(patientEntity.getGender())				
				.createdAt(patientEntity.getCreatedAt())
				.updatedAt(patientEntity.getUpdatedAt())
				.build();
	}

}//end class
