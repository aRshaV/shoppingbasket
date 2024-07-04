package com.interview.shoppingbasket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Basket {
    private List<BasketItem> items = new ArrayList<>();

    public void add(String productCode, String productName, int quantity) {
        BasketItem basketItem = new BasketItem();
        basketItem.setProductCode(productCode);
        basketItem.setProductName(productName);
        basketItem.setQuantity(quantity);

        items.add(basketItem);
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public void consolidateItems() {
        HashMap<String, BasketItem> itemMap = new HashMap<>();

        for (BasketItem item : items) {
            String pCode = item.getProductCode();
            if (itemMap.containsKey(item.getProductCode())) {
                BasketItem foundItem = itemMap.get(pCode);
                foundItem.setQuantity(foundItem.getQuantity() + item.getQuantity());
            } else {
                BasketItem newItem = new BasketItem();
                newItem.setProductCode(item.getProductCode());
                newItem.setProductName(item.getProductName());
                newItem.setQuantity(item.getQuantity());
                itemMap.put(item.getProductCode(), newItem);
            }
        }

        items = new ArrayList<>(itemMap.values());
    }
}
