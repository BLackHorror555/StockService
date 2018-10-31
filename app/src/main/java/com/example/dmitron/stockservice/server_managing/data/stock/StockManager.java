package com.example.dmitron.stockservice.server_managing.data.stock;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class StockManager {
    private static final String TAG = "StockManager";
    private static StockManager sStockManager;
    private final Stock mStock;
    private ArrayList<StockChangesListener> mListeners;
    private Random mRnd;

    private StockManager() {
        mStock = new Stock();
        mListeners = new ArrayList<>();
        mRnd = new Random();
    }

    public static StockManager getInstance() {
        if (sStockManager == null) {
            sStockManager = new StockManager();
        }
        return sStockManager;
    }

    /**
     * add new StockChangesListener
     *
     * @param stockChangesListener listener
     */
    public void addListener(StockChangesListener stockChangesListener) {
        mListeners.add(stockChangesListener);
    }

    private void notifyListeners() {
        for (StockChangesListener listener : mListeners) {
            listener.onProductsChanged();
        }
    }

    /**
     * Called when client want to buy product
     * increase interest
     * increase price
     *
     * @param type Enum product type
     * @return -1 if failed
     * selling price if success
     */
    public synchronized int buyProduct(ProductType type, int money) {
        int price;
        Product buyingProduct = mStock.mProducts.get(type);
        if (!mStock.mProducts.containsKey(type) || buyingProduct.getPrice() > money) {
            price = -1;
        } else {
            price = buyingProduct.getPrice();
            for (ProductType productType : ProductType.values()) {
                if (productType == type) {
                    buyingProduct.increaseInterest(mRnd.nextInt(3) + 1);
                    buyingProduct.increasePrice(mRnd.nextInt(7) + 4);
                    Log.i(TAG, "buyProduct: product " + type.name() + " was bought");
                } else {
                    mStock.mProducts.get(productType).decreaseInterest(mRnd.nextInt(2) + 1);
                    mStock.mProducts.get(productType).decreasePrice(mRnd.nextInt(4) + 1);
                }
            }
            notifyListeners();
        }
        return price;
    }

    /**
     * Called when client want to sell product
     * decrease interest
     * decrease price
     *
     * @param type Enum product type
     * @return price of sell item
     */
    public synchronized int sellProduct(ProductType type) {
        Product buyingProduct = mStock.mProducts.get(type);
        int sellingPrice = buyingProduct.getPrice();
        for (ProductType productType : ProductType.values()) {
            if (productType == type) {
                buyingProduct.decreaseInterest(2);
                buyingProduct.decreasePrice(5);
                Log.i(TAG, "buyProduct: product " + type.name() + " was sold");
            } else {
                mStock.mProducts.get(productType).increaseInterest(1);
                mStock.mProducts.get(productType).increasePrice(3);
            }

        }
        notifyListeners();
        return sellingPrice;

    }

    /**
     * creates json object from mProducts with fields product name (string) and price (int)
     *
     * @return json object
     */
    public synchronized JSONObject createJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<ProductType, Product> entry : getStock().getProducts().entrySet()) {
                ProductType productType = entry.getKey();
                Product product = entry.getValue();

                jsonObject.put(productType.name(), product.getPrice());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * get stock instance
     *
     * @return stock instance
     */
    public Stock getStock() {
        return mStock;
    }

    /**
     * implement to listen stock changes
     */
    public interface StockChangesListener {
        void onProductsChanged();
    }
}
