package com.orbsec.backendfileuploaderapi.repository;

import com.orbsec.backendfileuploaderapi.domain.DatabaseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DatabaseFileRepository extends JpaRepository<DatabaseFile, String> {

    public Optional<DatabaseFile> getFileByFileName(String fileName);
}
