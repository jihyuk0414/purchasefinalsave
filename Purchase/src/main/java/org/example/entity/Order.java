package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;



@Table(name = "orders")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderProductId.class)
public class Order {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Id
    @Column(name = "product_id")
    private Long productId ;

    @Column(name = "order_price")
    private int orderPrice ;

    @Column(name = "consumer_email")
    private String consumerEmail ;

    @Column(name = "seller_email")
    private String sellerEmail ;

    @Column(name = "order_at")
    private LocalDate orderAt;

}



