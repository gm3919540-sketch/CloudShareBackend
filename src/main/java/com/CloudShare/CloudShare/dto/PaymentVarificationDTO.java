package com.CloudShare.CloudShare.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class PaymentVarificationDTO {
    private String razorpay_order_id;
    private String razorpay_payment_id;
    private String razorpay_signatur;
    private String palnId;
}
