package com.example.strio01.notice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.notice.entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    @Query(value = """
            SELECT b.* FROM
                (SELECT rownum AS rm, a.* FROM
                    (SELECT * FROM NOTICE ORDER BY NOTICE_ID DESC)a
                )b
            WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
            """, nativeQuery = true)
    List<NoticeEntity> findPagedNoticeByRownum(@Param("pv") PageDTO pv);

    // 시퀀스 조회
    @Query(value = "SELECT NOTICE_ID_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    long getNextVal();

    // 단건 조회
    @Query("SELECT n FROM NoticeEntity n WHERE n.noticeId = :noticeId")
    NoticeEntity findWithNoticeByNoticeId(@Param("noticeId") long noticeId);

    // ❌ [삭제됨] getUploadFilename — upload 컬럼 없음
    // String getUploadFilename(long noticeId);

    // 게시글 수정
    @Modifying
    @Query("""
        UPDATE NoticeEntity n 
           SET n.title = :#{#noticeEntity.title},
               n.cont  = :#{#noticeEntity.cont},
               n.updatedAt = CURRENT_TIMESTAMP
         WHERE n.noticeId = :#{#noticeEntity.noticeId}
        """)
    void updateNotice(@Param("noticeEntity") NoticeEntity noticeEntity);
}
