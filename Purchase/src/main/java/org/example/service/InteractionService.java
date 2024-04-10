package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ValidationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final WebClient webClientforMember = WebClient.builder().baseUrl("http://localhost:8080/member").build() ;

    private final WebClient webClientforProduct =  WebClient.builder().baseUrl("http://localhost:7080/product").build();

    public Mono<?> changePointMember(ValidationRequest validationRequest)
    {
        Mono<?> responsemono = webClientforMember.post()
                .uri("미정")
                .retrieve()
                .bodyToMono(<?>) ;

        return responsemono;

    }

    public Mono<?> changeProductStatus(long productId)
    {
        Mono<?> responsemono = webClientforProduct.post()
                .uri("미정")
                .retrieve()
                .bodyToMono(<?>) ;

        return responsemono;

    }
}
