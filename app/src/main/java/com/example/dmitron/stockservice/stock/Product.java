package com.example.dmitron.stockservice.stock;

import java.util.Random;

public class Product {

    private static final int DEFAULT_PRODUCT_PRICE = 100;
    private static final int DEFAULT_PRODUCT_INTEREST = 50;

    private static final int MAX_INTEREST = 100;
    private static final int MIN_INTEREST = 0;

    private static final int MIN_PRICE = 10;



    private ProductType type;
    private int price;
    private int interestRate;

    Product(ProductType type){
        this.type = type;
        interestRate = DEFAULT_PRODUCT_INTEREST;
        price = DEFAULT_PRODUCT_PRICE + (new Random().nextInt(20) - 10);

    }

    Product(ProductType type, int price){
        this.type = type;
        interestRate = DEFAULT_PRODUCT_INTEREST;
        this.price = price;
    }

    public void increaseInterest(int value){
        interestRate += value;
        if (interestRate > MAX_INTEREST)
            interestRate = MAX_INTEREST;
    }

    public void decreaseInterest(int value){
        interestRate -= value;
        if (interestRate < MIN_INTEREST)
            interestRate = MIN_INTEREST;
    }

    public void increasePrice(int value){
        price += value;
    }

    public void decreasePrice(int value){
        price -= value;
        if (price < MIN_PRICE)
            price = MIN_PRICE;

    }

    public ProductType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }
}
