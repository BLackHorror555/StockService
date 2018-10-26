package com.example.dmitron.stockservice.server_managing;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServerManagingContract {

    interface View extends BaseView<Presenter>{
        void showConnectedClientsCount(int clientCount);
        void updateProductsInfo(JSONObject products) throws JSONException;
        void setStartButtonEnabling(boolean isVisible);
        void setStopButtonEnabling(boolean isVisible);
        void showProductAtGraph(ProductType productType);
        void showToastMessage(String msg);
    }

    interface Presenter extends BasePresenter{
        void startService();
        void stopService();
        void productToMonitoringSelected(ProductType productType);
        void detached();
        void viewCreated();
    }
}
