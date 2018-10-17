package com.example.dmitron.stockservice.Client;


import com.example.dmitron.stockservice.stock.ProductType;

import java.util.EnumMap;
import java.util.Map;

public class Trader {

    private static final int DEFAULT_MONEY = 500;
    private int money;

    /**
     * Map - Product type : number of products
     */
    private Map<ProductType, Integer> products;

    Trader(){
        money = DEFAULT_MONEY;
        products = new EnumMap<>(ProductType.class);
        products.put(ProductType.ORANGE, 1);
    }



    public void addProduct(ProductType productType){
        int count = products.containsKey(productType) ? products.get(productType) : 0;
        products.put(productType, ++count);
    }

    public boolean pickupProduct(ProductType productType){
        int count = products.containsKey(productType) ? products.get(productType) : 0;
        if (count == 0){
            return false;
        }
        else {
            products.put(productType, --count);
            return true;
        }
    }


    public void spendMoney(int money){
        this.money -= money;
    }

    public void increaseMoney(int money){
        this.money += money;
    }

    public Map<ProductType, Integer> getProducts() {
        return products;
    }

    public int getMoney() {
        return money;
    }
}
