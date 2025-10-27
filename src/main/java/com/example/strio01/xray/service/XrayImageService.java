package com.example.strio01.xray.service;

import java.util.List;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.xray.dto.XrayImageDTO;

public interface XrayImageService {
    long countProcess();
    List<XrayImageDTO> listProcess(PageDTO pv);
    void insertProcess(XrayImageDTO dto, String tempDir);
    XrayImageDTO contentProcess(long xrayId);
    void updateStatusProcess(XrayImageDTO dto);
    void deleteProcess(long xrayId, String tempDir);
}
