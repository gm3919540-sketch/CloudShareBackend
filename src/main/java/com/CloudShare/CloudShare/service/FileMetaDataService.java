package com.CloudShare.CloudShare.service;


import com.CloudShare.CloudShare.document.FileMetaDetaDocument;
import com.CloudShare.CloudShare.document.ProfileDocument;
import com.CloudShare.CloudShare.dto.FileMetaDataDTO;
import com.CloudShare.CloudShare.repository.FileMetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetaDataService {
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetaDataRepository fileMetaDataRepository;
    public List<FileMetaDataDTO> uploadFiles(MultipartFile files[]) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrrentProfile();
        List<FileMetaDetaDocument> savedFiles = new ArrayList<>();
         if(!userCreditsService.hasEnoughCredits(files.length)){
             throw  new RuntimeException("Not Enough credits to upload files. please purchase more credits");
         }
   Path uploadPath= Paths.get("upload").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        for(MultipartFile file :files){
       String fileName=  UUID.randomUUID()+"."+ StringUtils.getFilenameExtension(file.getOriginalFilename());
      Path targetLocation= uploadPath.resolve(fileName);
      Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);

      FileMetaDetaDocument fileMetadata = FileMetaDetaDocument.builder()
              .fileLocation(targetLocation.toString())
              .name(file.getOriginalFilename())
              .size(file.getSize())
              .type(file.getContentType())
              .clerkId(currentProfile.getClerkId())
              .isPublic(false)
              .uploadedAt(LocalDateTime.now())
              .build();
           //TODO: consume the  one credit for eahc file upload
           userCreditsService.consumeCredits();

            savedFiles.add(fileMetaDataRepository.save(fileMetadata));
        }
        return savedFiles.stream()
                .map(fileMetaDetaDocument -> mapToDTO(fileMetaDetaDocument))
                .collect(Collectors.toList());
    }

    private FileMetaDataDTO mapToDTO(FileMetaDetaDocument fileMetaDetaDocument) {
      return   FileMetaDataDTO.builder()
              .id(fileMetaDetaDocument.getId())
                .fileLocation(fileMetaDetaDocument.getFileLocation())
                .name(fileMetaDetaDocument.getName())
                .size(fileMetaDetaDocument.getSize())
                .type(fileMetaDetaDocument.getType())
                .clerkId(fileMetaDetaDocument.getClerkId())
                .isPublic(fileMetaDetaDocument.getIsPublic())
                .uploadedAt(fileMetaDetaDocument.getUploadedAt())
                .build();
    }
    public  List<FileMetaDataDTO> getFiles(){
        ProfileDocument currentProfile = profileService.getCurrrentProfile();
    List<FileMetaDetaDocument> files= fileMetaDataRepository.findByClerkId(currentProfile.getClerkId());
   return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    public FileMetaDataDTO getPublicFile(String id){
       Optional<FileMetaDetaDocument> fileOptional= fileMetaDataRepository.findById(id);
       if(fileOptional.isEmpty() || !fileOptional.get().getIsPublic()){
           throw new RuntimeException("Unable to get the file");
       }

           FileMetaDetaDocument document= fileOptional.get();
       return    mapToDTO(document);
    }
    public FileMetaDataDTO getDownloadableFile(String id){
    FileMetaDetaDocument file = fileMetaDataRepository.findById(id).orElseThrow(()->new RuntimeException("file not found"));
    return  mapToDTO(file);
    }
    public void deleteFile(String id){
        try{
       ProfileDocument currentProfile = profileService.getCurrrentProfile();
    FileMetaDetaDocument file= fileMetaDataRepository.findById(id)
              .orElseThrow(()->new RuntimeException("File not found"));
    if(!file.getClerkId().equals(currentProfile.getClerkId())){
        throw new RuntimeException("File is not belong to current user");
    }
    Path filePath= Paths.get(file.getFileLocation());
    fileMetaDataRepository.deleteById(id);
    Files.deleteIfExists(filePath);
        }catch (Exception e){
           throw  new RuntimeException("Error deleting the file");
        }
    }

    public FileMetaDataDTO togglePublic(String id){
      FileMetaDetaDocument file=  fileMetaDataRepository.findById(id).orElseThrow(()->new RuntimeException("File Not Found"));
      file.setIsPublic(!file.getIsPublic());
      fileMetaDataRepository.save(file);
      return mapToDTO(file);
    }

}
