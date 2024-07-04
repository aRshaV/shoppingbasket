package com.interview.shoppingbasket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CheckoutPipelineTest {

    CheckoutPipeline checkoutPipeline;


    @Mock
    Basket basket;

    @Mock
    CheckoutStep  basketStep;

    @Mock
    CheckoutStep promoStep;

    @Mock
    RetailPriceCheckoutStep retailStep;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        checkoutPipeline = new CheckoutPipeline();
    }

    @Test
    void returnZeroPaymentForEmptyPipeline() {
        PaymentSummary paymentSummary = checkoutPipeline.checkout(basket);

        assertEquals(paymentSummary.getRetailTotal(), 0.0);
    }

    @Test
    void executeAllPassedCheckoutSteps() {
        checkoutPipeline.addStep(basketStep);
        checkoutPipeline.addStep(promoStep);
        checkoutPipeline.addStep(retailStep);

        checkoutPipeline.checkout(basket);

        ArgumentCaptor<CheckoutContext> captor = ArgumentCaptor.forClass(CheckoutContext.class);

        Mockito.verify(basketStep).execute(captor.capture());
        Mockito.verify(promoStep).execute(captor.capture());
        Mockito.verify(retailStep).execute(captor.capture());
    }

}
