package com.CloudShare.CloudShare.controller;

import com.CloudShare.CloudShare.dto.PaymentDTO;
import com.CloudShare.CloudShare.dto.PaymentVarificationDTO;
import com.CloudShare.CloudShare.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentDTO paymentDTO){
        //call service method to creeate orrder
        PaymentDTO response= paymentService.createOrder(paymentDTO);
        if(response.getSuccess()){
            return ResponseEntity.ok(response);
        }else{
            return  ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVarificationDTO request){
        //use service method
         PaymentDTO response= paymentService.verifyPayment(request);
         if(response.getSuccess()){
             return ResponseEntity.ok(response);
         }else{
             return  ResponseEntity.badRequest().body(response);
         }
    }
}
