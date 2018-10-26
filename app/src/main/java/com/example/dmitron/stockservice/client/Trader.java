package com.example.dmitron.stockservice.client;


import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;

import java.util.EnumMap;
import java.util.Map;

public class Trader {

    private final int mId;
    private static int sLastId = 0;
    private static final int DEFAULT_MONEY = 500;
    private int mMoney;

    /**
     * Map - Product type : number of products
     */
    private Map<ProductType, Integer> products;

    public Trader(){
        mId = sLastId++;
        mMoney = DEFAULT_MONEY;
        products = new EnumMap<>(ProductType.class);
        products.put(ProductType.ORANGE, 1);
    }

    Trader(int money){
        mId = sLastId++;
        this.mMoney = money;
        products = new EnumMap<>(ProductType.class);
        products.put(ProductType.ORANGE, 1);
    }

    /**
     * get unique id of the trader
     * @return the trader id
     */
    public int getId(){
        return mId;
    }


    void addProduct(ProductType productType){
        int count = products.containsKey(productType) ? products.get(productType) : 0;
        products.put(productType, ++count);
    }

    /**
     * take the product from the trader
     * @param productType type of product
     * @return success
     */
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


    /**
     * check if trader has the product
     * @param productType type of product to check
     * @return has or not
     */
    boolean isHasProduct(ProductType productType){
        return products.containsKey(productType) && products.get(productType) > 0;
    }


    /**
     * takes mMoney
     * @param money the amount of mMoney to take
     */
    void spendMoney(int money){
        this.mMoney -= money;
    }

    /**
     * add mMoney
     * @param money amount of mMoney to add
     */
    void increaseMoney(int money){
        this.mMoney += money;
    }

    /**
     *
     * @return map product type : quantity oF products
     */
    public Map<ProductType, Integer> getProducts() {
        return products;
    }

    public int getMoney() {
        return mMoney;
    }
}
