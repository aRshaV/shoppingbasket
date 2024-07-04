package com.interview.shoppingbasket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
public abstract class Promotion {
    private String productCode;

    public abstract double getDiscount(BasketItem item, double price);
}
