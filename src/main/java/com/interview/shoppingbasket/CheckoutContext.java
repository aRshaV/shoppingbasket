package com.interview.shoppingbasket;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class CheckoutContext {
    @Getter
    private Basket basket;
    @Setter
    private double retailPriceTotal = 0.0;

    @Setter
    @Getter
    private HashMap<String, Promotion> promos = new HashMap<>();

    CheckoutContext(Basket basket) {
        this.basket = basket;
    }

    public PaymentSummary paymentSummary() {
        return new PaymentSummary(retailPriceTotal);
    }


}
