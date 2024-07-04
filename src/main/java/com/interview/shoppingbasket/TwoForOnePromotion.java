package com.interview.shoppingbasket;

import lombok.RequiredArgsConstructor;

public class TwoForOnePromotion extends Promotion {

    public TwoForOnePromotion(String productCode) {
        super(productCode);
    }

    @Override
    public double getDiscount(BasketItem item, double price) {
        int totalQuantity = item.getQuantity();
        int chargeableQuantity = totalQuantity / 2 + totalQuantity % 2;

        double totalPrice = chargeableQuantity * price;
        return totalPrice / totalQuantity;
    }
}
