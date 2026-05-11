package com.CloudShare.CloudShare.service;

import com.CloudShare.CloudShare.document.ProfileDocument;
import com.CloudShare.CloudShare.dto.ProfileDto;
import com.CloudShare.CloudShare.repository.ProfileRepository;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private  final ProfileRepository profileRepository;
    public ProfileDto createProfile(ProfileDto profileDto){
        log.info("inside createProfile");
        if(profileRepository.existsByClerkId(profileDto.getClerkId())){
            return updateProfile(profileDto);
        }
    ProfileDocument profile=    ProfileDocument.builder()
                .clerkId(profileDto.getClerkId())
                .email(profileDto.getEmail())
                .firstName(profileDto.getFirstName())
                .lastName(profileDto.getLastName())
                .photoUrl(profileDto.getPhotoUrl())
                .credits(5)
                .createdAt(Instant.now())
                .build();

        profile =profileRepository.save(profile);
      return ProfileDto.builder()
              .id(profile.getId())
              .clerkId(profile.getClerkId())
              .email(profile.getEmail())
              .firstName(profile.getFirstName())
              .lastName(profile.getLastName())
              .photoUrl(profile.getPhotoUrl())
              .credits(profile.getCredits())
              .createdAt(profile.getCreatedAt())
              .build();
    }
    public  ProfileDto updateProfile(ProfileDto profileDto){
      ProfileDocument existingProfile=profileRepository.findByClerkId(profileDto.getClerkId());
        if(existingProfile == null){
            return null;
        }
      if(existingProfile!=null){
          //update fields if provided
          if(profileDto.getEmail() !=null && !profileDto.getEmail().isEmpty()){
              existingProfile.setEmail(profileDto.getEmail());
          }
          if(profileDto.getFirstName()!=null && !profileDto.getFirstName().isEmpty()){
             existingProfile.setFirstName(profileDto.getFirstName());
          }
          if(profileDto.getLastName()!=null && !profileDto.getLastName().isEmpty()){
              existingProfile.setLastName(profileDto.getLastName());
          }

          if(profileDto.getPhotoUrl()!=null && !profileDto.getPhotoUrl().isEmpty()){
              existingProfile.setPhotoUrl(profileDto.getPhotoUrl());
          }
          profileRepository.save(existingProfile);

      }
        return ProfileDto.builder()
                .id(existingProfile.getId())
                .email(existingProfile.getEmail())
                .clerkId(existingProfile.getClerkId())
                .firstName(existingProfile.getFirstName())
                .lastName(existingProfile.getLastName())
                .credits(existingProfile.getCredits())
                .createdAt(existingProfile.getCreatedAt())
                .photoUrl(existingProfile.getPhotoUrl())
                .build();
    }

    public boolean existsByClerkId(String clerkId) {
        return profileRepository.existsByClerkId(clerkId);
    }
    public void deleteProfile(String clerkId){
      ProfileDocument existingProfile= profileRepository.findByClerkId(clerkId);
      if(existingProfile !=null){
          profileRepository.delete(existingProfile);
      }
    }
   public ProfileDocument getCurrrentProfile(){
        log.info("inside get profile");
        if(SecurityContextHolder.getContext().getAuthentication()==null){
            log.info("user not autheticated");
            throw  new UsernameNotFoundException("User not Authentication");

        }
        String clerkId = SecurityContextHolder.getContext().getAuthentication().getName();
       log.info("clerk Id in profile doc" +clerkId);
        return profileRepository.findByClerkId(clerkId);
    }
}
