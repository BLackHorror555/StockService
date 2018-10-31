package com.example.dmitron.stockservice.server_managing.server.messages;

import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

public class BuyingMessage extends Message{

    protected String mProductType;
    protected int mPrice;
    protected boolean success;

    public ProductType getProductType() {
        return ProductType.valueOf(mProductType);
    }

    public void setProductType(ProductType productType) {
        mProductType = productType.name();
    }

    public void setProductType(String productType) {
        mProductType = productType;
    }

    public int getPrice() {
        return mPrice;
    }

    public void setPrice(int price) {
        mPrice = price;
    }

    /**
     * @return is operation success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success success of done operation
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
