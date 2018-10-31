package com.example.dmitron.stockservice.server_managing.data.stock;

import java.util.Random;

public class Product {

    private static final int DEFAULT_PRODUCT_PRICE = 100;
    private static final int DEFAULT_PRODUCT_INTEREST = 50;

    private static final int MAX_INTEREST = 100;
    private static final int MIN_INTEREST = 0;
    private static final int MIN_PRICE = 10;

    private ProductType mType;
    private int mPrice;
    private int mInterestRate;

    Product(ProductType type){
        this.mType = type;
        mInterestRate = DEFAULT_PRODUCT_INTEREST;
        mPrice = DEFAULT_PRODUCT_PRICE + (new Random().nextInt(20) - 10);
    }

    Product(ProductType type, int price){
        this.mType = type;
        mInterestRate = DEFAULT_PRODUCT_INTEREST;
        this.mPrice = price;
    }

    /**
     * increase the interest rate of product
     * @param value how much to increase
     */
    void increaseInterest(int value){
        mInterestRate += value;
        if (mInterestRate > MAX_INTEREST)
            mInterestRate = MAX_INTEREST;
    }

    /**
     * decrease the interest rate of product
     * @param value how much to decrease
     */
    void decreaseInterest(int value){
        mInterestRate -= value;
        if (mInterestRate < MIN_INTEREST)
            mInterestRate = MIN_INTEREST;
    }
    /**
     * increase product price
     * @param value how much to increase
     */
    void increasePrice(int value){
        mPrice += value;
    }

    /**
     * decrease product price
     * @param value how much to decrease
     */
    void decreasePrice(int value){
        mPrice -= value;
        if (mPrice < MIN_PRICE)
            mPrice = MIN_PRICE;

    }

    /**
     * @return type of products (enum)
     */
    public ProductType getType() {
        return mType;
    }

    int getPrice() {
        return mPrice;
    }
}
