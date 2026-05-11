package com.CloudShare.CloudShare.controller;

import com.CloudShare.CloudShare.dto.ProfileDto;
import com.CloudShare.CloudShare.service.ProfileService;
import com.CloudShare.CloudShare.service.UserCreditsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class ClerkWebhookController {
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService; // when user is creted user is going to get some credits at start

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;
    @PostMapping("/clerk")
    public ResponseEntity<?> handdleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                 @RequestHeader("svix-timestamp") String svixTimestamp,
                                                 @RequestHeader("svix-signature") String svixSignature,
                                                 @RequestBody String payload
                                                 ) throws JsonProcessingException {
        log.error("inside webhook clerk");
        try{
        boolean isValid = verifyWebhookSignature(svixId,svixTimestamp,svixSignature,payload);
        if(!isValid){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
        }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
          String eventType =  rootNode.path("type").asText(); //it provide us with iinformation is user created deleted or updated

            switch ((eventType)){
                case "user.created":
                    log.info("user created");
                    handdleUserCreated(rootNode.path("data")); //all data is passed
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handdleUserDeleted(rootNode.path("data"));
                    break;
            }
            return  ResponseEntity.ok().build();
        }catch (Exception e){
            log.error("Webhook processing failed", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        }
    }

    private void handdleUserDeleted(JsonNode data) {
        String clerkId =data.path("id").asText();
        profileService.deleteProfile(clerkId);
    }

    private void handleUserUpdated(JsonNode data) {
        String clerkId =data.path("id").asText();
        String email ="";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && emailAddresses.size()>0){
            email = emailAddresses.get(0).path("email_address").asText();
        }
        String firstName=data.path("first_name").asText("");
        String lastName= data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDto updatedProfile =   ProfileDto.builder()
                .clerkId(clerkId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .photoUrl(photoUrl)
                .build();
        updatedProfile=  profileService.updateProfile(updatedProfile);
        if(updatedProfile == null){
            handdleUserCreated(data);
        }

    }

    private void handdleUserCreated(JsonNode data) {
        String clerkId = data.path("id").asText();
        String email="";
     JsonNode emailAddresses=  data.path("email_addresses");
     if(emailAddresses.isArray() && emailAddresses.size()>0){
         email = emailAddresses.get(0).path("email_address").asText();
     }
     String firstName=data.path("first_name").asText("");
    String lastName= data.path("last_name").asText("");
    String photoUrl = data.path("image_url").asText("");
     ProfileDto newProfile =   ProfileDto.builder()
                .clerkId(clerkId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .photoUrl(photoUrl)
                .build();
     profileService.createProfile(newProfile);
     userCreditsService.createIntitialCredits(clerkId);
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        //validate the signtaure
        return true;
    }
}
