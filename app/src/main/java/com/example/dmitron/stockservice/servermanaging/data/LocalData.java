package com.example.dmitron.stockservice.servermanaging.data;

import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class LocalData extends Observable {

    public enum Changed{
        CLIENT_COUNT, PRODUCTS
    }

    private int mConnectedClients;
    private static LocalData sInstance = new LocalData();
    private Map<ProductType, Integer> mProducts;

    private LocalData(){
        mProducts = new HashMap<>();
        mConnectedClients = 0;
    }

    public static LocalData getInstance(){
        return sInstance;
    }

    public int getClientsCount() {
        return mConnectedClients;
    }

    public void setConnectedClients(int connectedClients) {
        synchronized (this){
            this.mConnectedClients = connectedClients;
            notifyObservers(Changed.CLIENT_COUNT);
        }
    }

    public Map<ProductType, Integer> getProducts() {
        return mProducts;
    }

    public void setProducts(Map<ProductType, Integer> products) {
        synchronized (this) {
            this.mProducts = products;
            notifyObservers(Changed.PRODUCTS);
        }
    }
}
