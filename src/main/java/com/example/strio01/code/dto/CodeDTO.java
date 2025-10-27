package  com.example.strio01.code.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;
import com.example.strio01.code.entity.CodeEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
public class CodeDTO {
	private String codeGroup, codeId;
	private String  codeName, codeDesc, useYn;
	private Date createdAt, updatedAt;
	
	
	// DTO -> Entity
	public CodeEntity toEntity() {
		return CodeEntity.builder()
				.codeGroup(codeGroup)
				.codeId(codeId)
				.codeName(codeName)
				.codeDesc(codeDesc)
				.useYn(useYn)				
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
	
	
	// Entity -> DTO
	public static CodeDTO toDTO(CodeEntity codeEntity) {
		return CodeDTO.builder()
				.codeGroup(codeEntity.getCodeGroup())
				.codeId(codeEntity.getCodeId())
				.codeName(codeEntity.getCodeName())
				.codeDesc(codeEntity.getCodeDesc())
				.useYn(codeEntity.getUseYn())				
				.createdAt(codeEntity.getCreatedAt())
				.updatedAt(codeEntity.getUpdatedAt())
				.build();
	}

}//end class
