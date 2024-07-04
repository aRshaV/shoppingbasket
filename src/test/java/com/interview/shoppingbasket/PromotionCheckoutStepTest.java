package com.interview.shoppingbasket;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PromotionCheckoutStepTest {

    @Test
    void promotionCheckoutStepTest() {

        CheckoutContext checkoutContext = Mockito.mock(CheckoutContext.class);
        Basket basket = Mockito.mock(Basket.class);
        PromotionsService promotionsService = Mockito.mock(PromotionsService.class);

        when(checkoutContext.getBasket()).thenReturn(basket);

        PromotionCheckoutStep promotionCheckoutStep = new PromotionCheckoutStep(promotionsService);
        promotionCheckoutStep.execute(checkoutContext);

        Mockito.verify(checkoutContext).getBasket();
        Mockito.verify(promotionsService).getPromotions(basket);
    }
}
