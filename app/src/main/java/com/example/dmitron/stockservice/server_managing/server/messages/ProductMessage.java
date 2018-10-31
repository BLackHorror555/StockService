package com.example.dmitron.stockservice.server_managing.server.messages;

import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class ProductMessage extends Message {

    private String mJsonString;

    public String getJsonString() {
        return mJsonString;
    }

    /**
     *
     * @return products in message in map
     * @throws JSONException error parsing JSON
     */
    public Map<ProductType, Integer> getMapProducts() throws JSONException {

        JSONObject jsonProducts = new JSONObject(mJsonString);

        Map<ProductType, Integer> products = new EnumMap<>(ProductType.class);
        Iterator<String> iterator = jsonProducts.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            products.put(ProductType.valueOf(key), jsonProducts.getInt(key));
        }
        return products;
    }

    /**
     * @param jsonString products in json format
     */
    public void setProductsJson(JSONObject jsonString) {
        mJsonString = jsonString.toString();
    }

}
