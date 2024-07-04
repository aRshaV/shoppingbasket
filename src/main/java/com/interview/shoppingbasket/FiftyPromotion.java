package com.interview.shoppingbasket;

public class FiftyPromotion extends Promotion {

    public FiftyPromotion(String productCode) {
        super(productCode);
    }

    @Override
    public double getDiscount(BasketItem item, double price) {
        double discount = price * 0.5;
        return price - discount;
    }
}
