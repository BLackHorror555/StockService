package com.example.dmitron.stockservice.client.managed_client;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.util.Map;

public interface ManagedClientContract {
    interface View extends BaseView<Presenter>{
        void setCreateTraderButtonEnabled(boolean isEnable);
        void setKillTraderButtonEnabled(boolean isEnable);
        void setConnectButtonEnabled(boolean isEnable);
        void setDisconnectButtonEnabled(boolean isEnable);

        void showClientMoney(int money);
        void showClientProducts(Map<ProductType, Integer> products);
        void showStockProducts(Map<ProductType, Integer> products);
        void showToastMessage(String message);
        void cleanTraderInfo();
    }

    interface Presenter extends BasePresenter{
        /**
         * create new managed mTrader and connect to server
         * is success return in callback
         */
        void createManagedClient();
        void traderProductTapped(ProductType productType);
        void stockProductTapped(ProductType productType);
        void killManagedClient();
        void viewCreated();
        void connect();
        void disconnect();
    }
}
