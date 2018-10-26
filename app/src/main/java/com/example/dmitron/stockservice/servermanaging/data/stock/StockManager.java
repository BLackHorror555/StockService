package com.example.dmitron.stockservice.servermanaging.data.stock;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class StockManager {
    private static final String TAG = "StockManager";
    private static StockManager sStockManager;

    private StockManager() {
        mStock = new Stock();
    }

    private final Stock mStock;

    /**
     * Called when client want to buy product
     * increase interest
     * increase price
     *
     * @param type Enum product type
     * @return -1 if failed
     *          selling price if success
     */
    public synchronized int buyProduct(ProductType type) {
        int buyingPrice;
        Product buyingProduct = mStock.mProducts.get(type);
        if (!mStock.mProducts.containsKey(type))
            buyingPrice = -1;
        else {
            buyingPrice = buyingProduct.getPrice();
            for (ProductType productType : ProductType.values()) {
                if (productType == type){
                    buyingProduct.increaseInterest(2);
                    buyingProduct.increasePrice(5);
                    Log.i(TAG, "buyProduct: product " + type.name() + "was bought");
                }
                else{
                    mStock.mProducts.get(productType).decreaseInterest(1);
                    mStock.mProducts.get(productType).decreasePrice(3);
                }

            }

        }
        return buyingPrice;
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
            if (productType == type){
                buyingProduct.decreaseInterest(2);
                buyingProduct.decreasePrice(5);
                Log.i(TAG, "buyProduct: product " + type.name() + " was sold");
            }
            else{
                mStock.mProducts.get(productType).increaseInterest(1);
                mStock.mProducts.get(productType).increasePrice(3);
            }

        }
        return sellingPrice;

    }


    /**
     * creates json object from mProducts with fields product name (string) and price (int)
     * @return json object
     */
    public JSONObject createJson() {
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

    public static StockManager getInstance() {
        if (sStockManager == null) {
            sStockManager = new StockManager();
        }
        return sStockManager;
    }

    public Stock getStock() {
        return mStock;
    }
}
