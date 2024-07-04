package com.interview.shoppingbasket;

import java.util.HashMap;
import java.util.stream.Collectors;

public class PromotionCheckoutStep implements CheckoutStep {

    private PromotionsService promotionsService;

    public PromotionCheckoutStep(PromotionsService pricingService) {
        this.promotionsService = pricingService;
    }

    @Override
    public void execute(CheckoutContext checkoutContext) {
        Basket basket = checkoutContext.getBasket();
        checkoutContext.setPromos(promotionsService.getPromotions(basket).stream()
            .collect(Collectors.toMap(
                    Promotion::getProductCode,
                    promo -> promo,
                    (existing, replacement) -> existing,
                    HashMap::new
            )));
    }
}
