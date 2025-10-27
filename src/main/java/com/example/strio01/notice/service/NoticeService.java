package com.example.strio01.notice.service;

import java.util.List;

import com.example.strio01.notice.dto.NoticeDTO;
import com.example.strio01.board.dto.PageDTO;

public interface NoticeService {
	public long countProcess(); 
	public List<NoticeDTO> listProcess(PageDTO pv);
	public void insertProcess(NoticeDTO dto);
	public NoticeDTO contentProcess(long noticeId);
	public void updateProcess(NoticeDTO dto, String tempDir);
	public void deleteProcess(long noticeId, String tempDir);
}
