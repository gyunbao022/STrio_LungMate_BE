package com.example.strio01.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.code.entity.CodeEntity;
import com.example.strio01.code.entity.CodeId;

@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, CodeId> {

    @Query("SELECT c FROM CodeEntity c ORDER BY c.codeGroup, c.codeId")
    List<CodeEntity> findAllCodes();

    @Query("SELECT c FROM CodeEntity c WHERE c.codeGroup = :group AND c.codeId = :id")
    CodeEntity findCode(@Param("group") String group, @Param("id") String id);
}
