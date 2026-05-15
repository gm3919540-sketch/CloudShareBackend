package com.CloudShare.CloudShare.controller;

import com.CloudShare.CloudShare.document.UserCredits;
import com.CloudShare.CloudShare.dto.UserCreditDTO;
import com.CloudShare.CloudShare.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserCreditsController {
    private final UserCreditsService userCreditsService;
    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(){
      UserCredits userCredits= userCreditsService.getUserCredits();
     UserCreditDTO response=   UserCreditDTO.builder().credits(userCredits.getCredits())
                .plan(userCredits.getPlan())
                .build();
     return  ResponseEntity.ok(response);
    }
}
