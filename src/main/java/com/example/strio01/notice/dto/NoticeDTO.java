package  com.example.strio01.notice.dto;

import java.sql.Date;
import java.sql.Timestamp;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.notice.entity.NoticeEntity;

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
public class NoticeDTO {
	private Long noticeId;
	private String  title, cont, userId;
	private Date createdAt;
	private Timestamp updatedAt;
	

	// 클라이언트 MultipartFile 보냄 ->  DB에 저장 String 
	//board테이블의 파일 첨부를 처리해주는 멤버변수
	private String upload;

	//form페이지에서 파일첨부를 받아 처리해주는 멤버변수
	private MultipartFile filename;  
	
	// DTO -> Entity
	public NoticeEntity toEntity() {
		return NoticeEntity.builder()
				.noticeId(noticeId)
				.title(title)
				.cont(cont)
				.userId(userId)				
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
	
	
	// Entity -> DTO
	public static NoticeDTO toDTO(NoticeEntity noticeEntity) {
		return NoticeDTO.builder()
				.noticeId(noticeEntity.getNoticeId())
				.title(noticeEntity.getTitle())
				.cont(noticeEntity.getCont())
				.userId(noticeEntity.getUserId())				
				.createdAt(noticeEntity.getCreatedAt())
				.updatedAt(noticeEntity.getUpdatedAt())
				.build();
	}

}//end class
