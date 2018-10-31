package com.example.dmitron.stockservice.server_managing.data.stock;

import java.util.EnumMap;
import java.util.Map;

class Stock {

    Map<ProductType, Product> mProducts = new EnumMap<ProductType, Product>(ProductType.class);

    Stock() {
        initStock();
    }

    /**
     * initialise stock with default products
     */
    private void initStock() {
        for (ProductType type : ProductType.values()) {
            Product product = new Product(type);
            mProducts.put(type, product);
        }
    }

    /**
     * @param type Enum type of product
     * @return true - success, false - if not
     */
    boolean AddProduct(ProductType type) {
        boolean result;
        if (mProducts.containsKey(type)) {
            result = false;
        } else {
            mProducts.put(type, new Product(type));
            result = true;
        }
        return result;
    }

    /**
     * get products
     *
     * @return map of products (product type : product)
     */
    Map<ProductType, Product> getProducts() {
        return mProducts;
    }

    /**
     * delete product of specific type from stock
     *
     * @param type type of product
     * @return success
     */
    boolean deleteProduct(ProductType type) {
        return mProducts.remove(type) != null;
    }
}
