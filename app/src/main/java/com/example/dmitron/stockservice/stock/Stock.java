package com.example.dmitron.stockservice.stock;

import java.util.EnumMap;
import java.util.Map;

public class Stock {

    Map<ProductType, Product> products = new EnumMap<ProductType, Product>(ProductType.class);

    Stock(){
        initStock();
    }

    private void initStock() {
        for (ProductType type : ProductType.values()){
            Product product = new Product(type);
            products.put(type, product);
        }
    }

    /**
     *
     * @param type Enum type of product
     * @return  true - success, false - if not
     */
    public boolean AddProduct(ProductType type){
        boolean result;
        if (products.containsKey(type)){
            result = false;
        } else {
            products.put(type, new Product(type));
            result = true;
        }
        return result;
    }


    public Map<ProductType, Product> getProducts() {
        return products;
    }

    public boolean deleteProduct(ProductType type){
        return products.remove(type) != null;
    }
}
