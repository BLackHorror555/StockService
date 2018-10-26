package com.example.dmitron.stockservice.servermanaging.data.stock;

import java.util.EnumMap;
import java.util.Map;

public class Stock {

    Map<ProductType, Product> mProducts = new EnumMap<ProductType, Product>(ProductType.class);

    Stock(){
        initStock();
    }

    private void initStock() {
        for (ProductType type : ProductType.values()){
            Product product = new Product(type);
            mProducts.put(type, product);
        }
    }

    /**
     *
     * @param type Enum type of product
     * @return  true - success, false - if not
     */
    public boolean AddProduct(ProductType type){
        boolean result;
        if (mProducts.containsKey(type)){
            result = false;
        } else {
            mProducts.put(type, new Product(type));
            result = true;
        }
        return result;
    }


    public Map<ProductType, Product> getProducts() {
        return mProducts;
    }

    public boolean deleteProduct(ProductType type){
        return mProducts.remove(type) != null;
    }
}
