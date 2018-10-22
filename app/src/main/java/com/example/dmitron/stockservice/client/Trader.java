package com.example.dmitron.stockservice.client;


import com.example.dmitron.stockservice.stock.ProductType;

import java.util.EnumMap;
import java.util.Map;

public class Trader {

    private final int ID;
    private static int lastID = 0;
    private static final int DEFAULT_MONEY = 500;
    private int money;

    /**
     * Map - Product type : number of products
     */
    private Map<ProductType, Integer> products;

    public Trader(){
        ID = lastID++;
        money = DEFAULT_MONEY;
        products = new EnumMap<>(ProductType.class);
        products.put(ProductType.ORANGE, 1);
    }

    Trader(int money){
        ID = lastID++;
        this.money = money;
        products = new EnumMap<>(ProductType.class);
        products.put(ProductType.ORANGE, 1);
    }

    int getID(){
        return ID;
    }


    void addProduct(ProductType productType){
        int count = products.containsKey(productType) ? products.get(productType) : 0;
        products.put(productType, ++count);
    }

    boolean pickupProduct(ProductType productType){
        int count = products.containsKey(productType) ? products.get(productType) : 0;
        if (count == 0){
            return false;
        }
        else {
            products.put(productType, --count);
            return true;
        }
    }

    boolean isHasProduct(ProductType productType){
        return products.containsKey(productType);
    }


    void spendMoney(int money){
        this.money -= money;
    }

    void increaseMoney(int money){
        this.money += money;
    }

    public Map<ProductType, Integer> getProducts() {
        return products;
    }

    public int getMoney() {
        return money;
    }
}
