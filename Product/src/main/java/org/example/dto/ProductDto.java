package org.example.dto;

import lombok.*;
import org.example.entity.Product;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor

public class ProductDto {

    private Long product_id ;

    private String product_name;

    private int price ;

    private String image_product;
    private String image_real;

    private int state;

    private int category_id;

    private LocalDate create_at;

    private LocalDate expire_at;

    private String nick_name;
    private String userProfile;
    @Builder
    public ProductDto(Long product_id,String nick_name,String product_name, int price, LocalDate create_at, LocalDate expire_at, String image_product, String image_real, int category_id, String userProfile){
        this.category_id=category_id;
        this.expire_at=expire_at;
        this.image_product=image_product;
        this.product_name=product_name;
        this.create_at=create_at;
        this.price=price;
        this.image_real=image_real;
        this.nick_name=nick_name;
        this.product_id=product_id;
        this.userProfile=userProfile;
    }
    public static ProductDto ToDto(Product product){
        return ProductDto.builder()
                .nick_name(product.getNickName())
                .product_id(product.getProductId())
                .category_id(product.getCategoryId())
                .expire_at(product.getExpireAt())
                .create_at(product.getCreateAt())
                .image_product(product.getImageProduct())
                .image_real(product.getImageReal())
                .product_name(product.getProductName())
                .price(product.getPrice())
                .userProfile(product.getUserProfile())
                .build();
    }

}
