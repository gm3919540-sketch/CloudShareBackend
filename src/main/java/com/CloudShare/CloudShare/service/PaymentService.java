package com.CloudShare.CloudShare.service;

import com.CloudShare.CloudShare.document.PaymentTransaction;
import com.CloudShare.CloudShare.document.ProfileDocument;
import com.CloudShare.CloudShare.dto.PaymentDTO;
import com.CloudShare.CloudShare.dto.PaymentVarificationDTO;
import com.CloudShare.CloudShare.repository.PaymentTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Formatter;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${razorpay.key.id}")
   private  String razorepayKeyId;
    @Value("${razorpay.key.secret}")
   private String razorpaySecret;

   public PaymentDTO createOrder(PaymentDTO paymentDTO){
       try{
          ProfileDocument currentProfile= profileService.getCurrrentProfile();
          String clerkId= currentProfile.getClerkId();
          RazorpayClient razorpayClient = new RazorpayClient(razorepayKeyId,razorpaySecret);
           JSONObject orderRequeest = new JSONObject();
           orderRequeest.put("amount",paymentDTO.getAmount());
           orderRequeest.put("currency",paymentDTO.getCurrency());
           orderRequeest.put("receipt","order_"+System.currentTimeMillis());
           Order order= razorpayClient.orders.create(orderRequeest);
            String orderId = order.get("id");
            //create pending transaction record
           PaymentTransaction transaction = PaymentTransaction.builder()
                   .clerkId(clerkId)
                   .orderId(orderId)
                   .planId(paymentDTO.getPlaneId())
                   .amount(paymentDTO.getAmount())
                   .currency(paymentDTO.getCurrency())
                   .status("PENDING")
                   .transactionDate(LocalDateTime.now())
                   .userEmail(currentProfile.getEmail())
                   .username(currentProfile.getFirstName()+" "+ currentProfile.getLastName())
                   .build();
           paymentTransactionRepository.save(transaction);
           return PaymentDTO.builder()
                   .orderId(orderId)
                   .success(true)
                   .message("Order created succefully")
                   .build();
       }catch (Exception e){
         return  PaymentDTO.builder()
                 .success(false)
                 .message("Error creating order: "+e.getMessage())
                 .build();
       }

   }
   
    public PaymentDTO verifyPayment(PaymentVarificationDTO request){
         try{
             ProfileDocument currrentProfile= profileService.getCurrrentProfile();
             String clerkId = currrentProfile.getClerkId();
           String data=  request.getRazorpay_payment_id()+"|" +request.getRazorpay_payment_id();
           String generatedsignature=  generateHmacSha256Signature(data,razorpaySecret);
           if(generatedsignature.equals(request.getRazorpay_signatur())){
               updateTransactionStatus(request.getRazorpay_order_id(),"FAILED",request.getRazorpay_payment_id(),null);
               return PaymentDTO.builder()
                       .success(false)
                       .message("Payment signature verification failed")
                       .build();
           }
           //add credits base on plan
             int creditsToAdd=0;
           String plan ="BASOC";
           switch (request.getPalnId()){
               case "premium":
                   creditsToAdd =500;
                   plan ="PREMIUM";
                   break;
               case "ultimate":
                   creditsToAdd=5000;
                   plan="UTIMATE";
                   break;
           }
           if(creditsToAdd>0){
               userCreditsService.addCredits(clerkId,creditsToAdd,plan);
               updateTransactionStatus(request.getRazorpay_order_id(),"SUCCESS",request.getRazorpay_payment_id(),creditsToAdd);
               return  PaymentDTO.builder()
                       .success(true)
                       .message("Payment verified and credits added succedssfully")
                       .credits(userCreditsService.getUserCredits(clerkId).getCredits())
                       .build();
           }else{
               updateTransactionStatus(request.getRazorpay_order_id(),"FAILED",request.getRazorpay_payment_id(),null);
               return PaymentDTO.builder().success(false)
                       .message("Invalid plan selected")
                       .build();
           }
         }catch (Exception e){
            try{
                updateTransactionStatus(request.getRazorpay_order_id(),"ERROR", request.getRazorpay_payment_id(), null);
            } catch (Exception ex){
                throw new RuntimeException(ex);
            }
            return PaymentDTO.builder()
                    .success(false)
                    .message("Error verifying payment:"+e.getMessage())
                    .build();

         }

    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer CREDITSTOADD) {
       paymentTransactionRepository.findAll().stream()
               .filter(t->t.getOrderId()!=null && t.getOrderId().equals(razorpayOrderId)).findFirst()
               .map(transaction->{
                   transaction.setStatus(status);
                   transaction.setPaymentId(razorpayPaymentId);
                   if(CREDITSTOADD!=null){
                       transaction.setCreditsAdded(CREDITSTOADD);
                   }
                   return paymentTransactionRepository.save(transaction);
               })
               .orElse(null);
    }
//      * Generate HMAC SHA256 signature for payment verification
//     */
    private String generateHmacSha256Signature(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);

        byte[] hmacData = mac.doFinal(data.getBytes());

        return toHexString(hmacData);
    }

    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
