package com.example.strio01.notice.service;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.notice.dto.NoticeDTO;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.notice.entity.NoticeEntity;
import com.example.strio01.notice.repository.NoticeRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoticeServiceImp implements NoticeService {
	@Autowired
	private NoticeRepository noticeRepository;

	public NoticeServiceImp() {

	}

	@Transactional
	@Override
	public long countProcess() {
		long cnt = noticeRepository.count();
		return cnt;
	}

	@Transactional
	@Override
	public List<NoticeDTO> listProcess(PageDTO pv) {
		List<NoticeEntity> listNoticeEntity = noticeRepository.findPagedNoticeByRownum(pv);
		List<NoticeDTO> listNoticeDTO = listNoticeEntity.stream().map(NoticeDTO::toDTO).collect(Collectors.toList());

		return listNoticeDTO;
	}

	@Transactional
	@Override
	public void insertProcess(NoticeDTO dto) {
		long newId = noticeRepository.getNextVal(); // 별도 시퀀스 조회 필요
		dto.setNoticeId(newId);

		dto.setCreatedAt(new Date(System.currentTimeMillis()));

		NoticeEntity noticeEntity = dto.toEntity();
		noticeRepository.save(noticeEntity);
	}

	@Transactional
	@Override
	public NoticeDTO contentProcess(long noticeId) {

		NoticeEntity noticeEntity = noticeRepository.findWithNoticeByNoticeId(noticeId);
		return NoticeDTO.toDTO(noticeEntity);
	}

	@Transactional
	@Override
	public void updateProcess(NoticeDTO dto, String tempDir) {
	    // 업로드 컬럼이 없으므로 파일 삭제 관련 로직 제거
	    NoticeEntity noticeEntity = dto.toEntity();
	    noticeRepository.updateNotice(noticeEntity);
	}

	@Transactional
	@Override
	public void deleteProcess(long noticeId, String tempDir) {
	    // 파일첨부 없음 → 바로 DB에서 삭제
	    noticeRepository.deleteById(noticeId);
	}
}





