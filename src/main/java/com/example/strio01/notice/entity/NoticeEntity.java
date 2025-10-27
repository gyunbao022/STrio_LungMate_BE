package com.example.strio01.notice.entity;

import java.sql.Date;
import java.sql.Timestamp;

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
@Table(name = "notice")
public class NoticeEntity {
	@Id
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_seq_generator")
	//@SequenceGenerator(name = "board_seq_generator", sequenceName = "board_num_seq", allocationSize = 1)
	private Long noticeId;
	private String  title, cont, userId;
	private Date createdAt;
	private Timestamp updatedAt;
}
