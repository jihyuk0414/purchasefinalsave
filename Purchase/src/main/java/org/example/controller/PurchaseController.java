package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PurChaseCheck;
import org.example.dto.ValidationBucketRequest;
import org.example.dto.ValidationRequest;
import org.example.service.AccessTokenService;
import org.example.service.InteractionService;
import org.example.service.PurchaseService;
import org.example.service.ValidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PurchaseController {

    private final AccessTokenService accessTokenService ;

    private final ValidateService validateService;

    private final PurchaseService purchaseService;

    private final InteractionService interactionService ;
    //paymentservice는 portone의 결제 확인 정보를 포함한 내용을
    //purchase는 결제 확인 정보와 검증을 포함한 내용을 전달해야 합니다.


    //한개 결제시 .

    @PostMapping("/payments/complete/{useremail}")
    public Mono<ResponseEntity<PurChaseCheck>> validatepaymentone(@PathVariable(value = "useremail") String useremail, @RequestBody ValidationRequest validation) {
        return accessTokenService.GetToken()
                .flatMap(PortOnetoken -> validateService.getpurchaseinfobyportone(validation.getPayment_id(), PortOnetoken)
                        .flatMap(purchasecheckresponsewebclient -> purchaseService.validateandsave(purchasecheckresponsewebclient,validation.getPayment_id(),validation.getTotal_amount(),useremail)
                                .flatMap(changememberpoint -> interactionService.changePointMember(validation)
                                        .flatMap(changeproductstatus -> interactionService.changeProductStatus(validation.getProduct_id())
                                                .flatMap()))
                        }));
    }

    @PostMapping("/payments/complete/bucket/{useremail}")
    public Mono<ResponseEntity<PurChaseCheck>> validatepaymentmany(@PathVariable(value = "useremail") String useremail, @RequestBody ValidationBucketRequest validation) {
        return accessTokenService.GetToken()
                .flatMap(PortOnetoken -> validateService.getpurchaseinfobyportone(validation.getPayment_id(), PortOnetoken)
                        .flatMap(purchasecheckresponsewebclient -> {
                            return purchaseService.validateandsave(purchasecheckresponsewebclient,validation.getPayment_id(),validation.getTotal_amount(),useremail);
                        }));
    }


}
