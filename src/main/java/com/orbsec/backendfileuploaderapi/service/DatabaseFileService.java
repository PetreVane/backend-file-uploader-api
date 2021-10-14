package com.orbsec.backendfileuploaderapi.service;

import com.orbsec.backendfileuploaderapi.domain.DatabaseFile;
import com.orbsec.backendfileuploaderapi.exception.FileNotFoundException;
import com.orbsec.backendfileuploaderapi.repository.DatabaseFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DatabaseFileService {

    private final DatabaseFileRepository databaseFileRepository;

    @Autowired
    public DatabaseFileService(DatabaseFileRepository databaseFileRepository) {
        this.databaseFileRepository = databaseFileRepository;
    }

    public void saveFile(MultipartFile file) throws IOException {
        var fileName = file.getOriginalFilename();
        var fileToBeSaved = new DatabaseFile(fileName, file.getContentType(), file.getBytes());
        databaseFileRepository.save(fileToBeSaved);
    }


    public DatabaseFile getFileByName(String fileName) {
        var existingFile = databaseFileRepository.getFileByFileName(fileName);
        if (existingFile.isPresent()) {
            return existingFile.get();
        } else {
            throw new FileNotFoundException(String.format("The file '%s' could not be located or is not readable", fileName));
        }
    }


    public Stream<DatabaseFile> getAllFiles() {
        return databaseFileRepository.findAll().stream();
    }

    public void deleteFileByName(String fileName) throws FileNotFoundException {
        var fileToBeDeleted = getFileByName(fileName);
        databaseFileRepository.delete(fileToBeDeleted);
    }

    public List<String> deleteAllFiles() {
        List<String> fileNames = new ArrayList<>();
        var filesToBeDeleted = getAllFiles();
        filesToBeDeleted.forEach(databaseFile -> fileNames.add(databaseFile.getFileName()));
        databaseFileRepository.deleteAll();
        return fileNames;
    }
}
