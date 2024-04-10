package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationRequest {

    String payment_id;
    int difference_amount; //차이 금액
    Long product_id;
    int original_amount ; //상품 원본 금액


}
