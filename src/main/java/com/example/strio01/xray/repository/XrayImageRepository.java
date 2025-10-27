package com.example.strio01.xray.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.xray.entity.XrayImageEntity;

@Repository
public interface XrayImageRepository extends JpaRepository<XrayImageEntity, Long> {

    @Query(value = """
            SELECT b.* FROM (
                SELECT rownum AS rm, a.* 
                FROM (SELECT * FROM XRAY_IMAGE ORDER BY XRAY_ID DESC) a
            ) b
            WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
            """, nativeQuery = true)
    List<XrayImageEntity> findPagedXrayByRownum(@Param("pv") PageDTO pv);

    @Query(value = "SELECT xray_id_seq.NEXTVAL FROM dual", nativeQuery = true)
    long getNextVal();

    @Query(value = "SELECT * FROM XRAY_IMAGE WHERE XRAY_ID = :xrayId", nativeQuery = true)
    XrayImageEntity findByXrayId(@Param("xrayId") long xrayId);

    @Modifying
    @Query("""
            UPDATE XrayImageEntity x
            SET x.statusCd = :#{#entity.statusCd},
                x.updatedAt = :#{#entity.updatedAt}
            WHERE x.xrayId = :#{#entity.xrayId}
            """)
    void updateStatus(@Param("entity") XrayImageEntity entity);
}
