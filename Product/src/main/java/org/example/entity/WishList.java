package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Entity
@Data
@RequiredArgsConstructor
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishListId;

    @ManyToOne
    @JoinColumn(name = "product")
    private Product product;

    @Column(name = "email")
    private String email;

    @Builder
    public WishList(String email, Product product){
        this.email=email;
        this.product=product;
    }
}
