package com.interview.shoppingbasket;

public class TenPromotion extends Promotion {
    public TenPromotion(String productCode) {
        super(productCode);
    }

    @Override
    public double getDiscount(BasketItem item, double price) {
        double discount = price * 0.1;
        return price - discount;
    }
}
