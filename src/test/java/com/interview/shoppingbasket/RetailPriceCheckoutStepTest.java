package com.interview.shoppingbasket;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class RetailPriceCheckoutStepTest {


    Promotion promotion;
    PricingService pricingService;
    CheckoutContext checkoutContext;
    Basket basket;

    @BeforeEach
    void setup() {
        pricingService = Mockito.mock(PricingService.class);
        checkoutContext = Mockito.mock(CheckoutContext.class);
        basket = new Basket();
        promotion = Mockito.mock(Promotion.class);
        when(checkoutContext.getBasket()).thenReturn(basket);
    }

    @Test
    void setPriceZeroForEmptyBasket() {

        RetailPriceCheckoutStep retailPriceCheckoutStep = new RetailPriceCheckoutStep(pricingService);

        retailPriceCheckoutStep.execute(checkoutContext);

        Mockito.verify(checkoutContext).setRetailPriceTotal(0.0);
    }

    @Test
    void setPriceOfProductToBasketItem() {

        basket.add("product1", "myproduct1", 10);
        basket.add("product2", "myproduct2", 10);

        when(pricingService.getPrice("product1")).thenReturn(3.99);
        when(pricingService.getPrice("product2")).thenReturn(2.0);
        RetailPriceCheckoutStep retailPriceCheckoutStep = new RetailPriceCheckoutStep(pricingService);
        when(retailPriceCheckoutStep.applyPromotion(promotion, basket.getItems().get(0), 3.99 )).thenReturn(3.99);
        when(retailPriceCheckoutStep.applyPromotion(promotion, basket.getItems().get(1), 2.0 )).thenReturn(2.0);

        retailPriceCheckoutStep.execute(checkoutContext);
        Mockito.verify(checkoutContext).setRetailPriceTotal(3.99*10+2*10);

    }

    @Test
    void shouldApplyPromotionsOnTotalPrice() {

        HashMap<String,Promotion> promotions = new HashMap<>();
        promotions.put("product1", new TenPromotion("product1"));
        promotions.put("product2", new TwoForOnePromotion("product2"));
        promotions.put("product3", new FiftyPromotion("product3"));

        basket.add("product1", "myproduct1", 1);
        basket.add("product2", "myproduct2", 2);
        basket.add("product3", "myproduct3", 1);
        basket.add("product4", "myproduct4", 1);


        when(pricingService.getPrice("product1")).thenReturn(4.0);
        when(pricingService.getPrice("product2")).thenReturn(2.0);
        when(pricingService.getPrice("product3")).thenReturn(2.0);
        when(pricingService.getPrice("product4")).thenReturn(5.0);

        when(checkoutContext.getPromos()).thenReturn(promotions);
        when(checkoutContext.getPromos()).thenReturn(promotions);
        when(checkoutContext.getPromos()).thenReturn(promotions);

        RetailPriceCheckoutStep retailPriceCheckoutStep = new RetailPriceCheckoutStep(pricingService);
        retailPriceCheckoutStep.execute(checkoutContext);
        Mockito.verify(checkoutContext).setRetailPriceTotal(3.6+2+1+5);
    }


}
