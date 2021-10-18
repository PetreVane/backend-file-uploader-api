package com.orbsec.backendfileuploaderapi.controller;

import com.orbsec.backendfileuploaderapi.exception.FileNotFoundException;
import com.orbsec.backendfileuploaderapi.dto.ResponseFile;
import com.orbsec.backendfileuploaderapi.dto.ResponseMessage;
import com.orbsec.backendfileuploaderapi.service.DatabaseFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;


@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost")
public class FileController {

    private final DatabaseFileService fileService;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final Environment environment;

    @Autowired
    public FileController(DatabaseFileService fileService, Environment environment) {
        this.fileService = fileService;
        this.environment = environment;
    }

    @GetMapping("/status")
    public String getStatus() {
        var portNumber = environment.getProperty("local.server.port");
        logger.info("Call to Microservice Status");
        return "FileUploader microservice is up and running on port " + portNumber;
    }


    @PostMapping(path ="/upload", consumes = {MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ResponseMessage> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        String message;
        try {
            List<String> fileNames = new ArrayList<>();
            Arrays.stream(files).forEach(file -> {
                try {
                    fileService.saveFile(file);
                    fileNames.add(file.getOriginalFilename());
                    logger.info(String.format("File saved: '%s'", file.getOriginalFilename()));
                } catch (IOException e) {
                    logger.warn(e.getLocalizedMessage());
                }
            });
            message = "Uploaded to database: " + fileNames;
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Upload failed!" ;
            logger.warn(message);
            logger.error(e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }

    }

    @GetMapping("/files")
    public ResponseEntity<List<ResponseFile>> getAllFiles() {
        var files = fileService.getAllFiles().map(databaseFile -> {
            var downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/")
                    .path(databaseFile.getId())
                    .toUriString();
            logger.info(String.format("File retrieved: '%s'", databaseFile.getFileName()));
            return new ResponseFile(databaseFile.getFileName(), downloadUrl, databaseFile.getFileType());
        }).collect(Collectors.toList());
        logger.info(String.format("All files retreived"));
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<byte[]> getFileByName(@PathVariable String fileName) {
        var existingFile = fileService.getFileByName(fileName);
        if (existingFile != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename =\"" + existingFile.getFileName() + "\"")
                    .body(existingFile.getData());
        } else {
            return  (ResponseEntity<byte[]>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<String>> deleteAllFiles() {
        var deletedFiles = fileService.deleteAllFiles();
        return ResponseEntity.status(HttpStatus.OK).body(deletedFiles);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFileByName(@PathVariable String fileName) {
        String message;
        try {
            fileService.deleteFileByName(fileName);
            message = String.format("File %s has been deleted", fileName);
            logger.info(message);
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (FileNotFoundException exception) {
            logger.warn(exception.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getLocalizedMessage());
        }
    }

}
