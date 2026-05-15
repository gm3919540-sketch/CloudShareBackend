package com.CloudShare.CloudShare.service;

import com.CloudShare.CloudShare.document.UserCredits;
import com.CloudShare.CloudShare.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditsService {
    private final UserCreditsRepository userCreditsRepository;
    private final ProfileService profileService;
    public UserCredits createIntitialCredits(String clerkId){
     UserCredits userCredits=   UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("Basic")
                .build();

    return  userCreditsRepository.save(userCredits);
    }
    public UserCredits getUserCredits(String clerkId){
   return  userCreditsRepository.findByClerkId(clerkId).orElseGet(()->createIntitialCredits(clerkId));
    }
    public UserCredits getUserCredits(){
      String clerkId=  profileService.getCurrrentProfile().getClerkId();
      return  getUserCredits(clerkId);
    }
    public Boolean hasEnoughCredits(int requiredCredits){
       UserCredits userCredits= getUserCredits();
      return userCredits.getCredits() >= requiredCredits;
    }
    public UserCredits consumeCredits(){
      UserCredits userCredits=getUserCredits();
      if(userCredits.getCredits() <=0){
          return null;
      }
      userCredits.setCredits(userCredits.getCredits()-1);
       return userCreditsRepository.save(userCredits);
    }
    public UserCredits addCredits(String clerkId,Integer creditsToAdd,String plan){
     UserCredits userCredits=    userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(()->createIntitialCredits(clerkId));
     userCredits.setCredits(userCredits.getCredits() +creditsToAdd);
     userCredits.setPlan(plan);
      return userCreditsRepository.save(userCredits);
    }
}
