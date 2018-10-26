package com.example.dmitron.stockservice.servermanaging;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;

import java.util.Map;

public interface ServerManagingContract {

    interface View extends BaseView<Presenter>{
        void showConnectedClientsCount(int clientCount);
        void updateProductsInfo(Map<ProductType, Integer> products);
        void setStartButtonEnabling(boolean isVisible);
        void setStopButtonEnabling(boolean isVisible);
        void showProductAtGraph(ProductType productType);
    }

    interface Presenter extends BasePresenter{
        void startService();
        void stopService();
        void productToMonitoringSelected(ProductType productType);
        void detached();
    }
}
