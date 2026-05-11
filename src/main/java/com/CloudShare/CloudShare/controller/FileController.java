package com.CloudShare.CloudShare.controller;

import com.CloudShare.CloudShare.document.UserCredits;
import com.CloudShare.CloudShare.dto.FileMetaDataDTO;
import com.CloudShare.CloudShare.service.FileMetaDataService;
import com.CloudShare.CloudShare.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {
    private final FileMetaDataService fileMetaDataService;
    private final UserCreditsService userCreditsService;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("files")MultipartFile files[]) throws IOException {
        Map<String,Object> response = new HashMap<>();
        List<FileMetaDataDTO> list = fileMetaDataService.uploadFiles(files);
       UserCredits finalCredits = userCreditsService.getUserCredits();
        response.put("files",list);
        response.put("remainingCredits",finalCredits.getCredits());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/my")
    public ResponseEntity<?> getFilesCurrentUser(){
       List<FileMetaDataDTO> files = fileMetaDataService.getFiles();
       return ResponseEntity.ok(files);
    }
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
        FileMetaDataDTO file = fileMetaDataService.getPublicFile(id);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException{
        log.info("insode downlod file");
      FileMetaDataDTO downloadableFile = fileMetaDataService.getDownloadableFile(id);
       Path path = Paths.get(downloadableFile.getFileLocation());
      Resource resource= new UrlResource(path.toUri());
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
              .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+downloadableFile.getName()+"\"")
              .body(resource);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id){
              fileMetaDataService.deleteFile(id);
              return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id){
        FileMetaDataDTO file = fileMetaDataService.togglePublic(id);
        return ResponseEntity.ok(file);
    }
}
