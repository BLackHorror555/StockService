package com.example.dmitron.stockservice.client.managed_client;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.util.Map;

public interface ManagedClientContract {
    interface View extends BaseView<Presenter>{
        /**
         * set enabling of button
         * @param isEnable is enable
         */
        void setCreateTraderButtonEnabled(boolean isEnable);
        void setKillTraderButtonEnabled(boolean isEnable);
        void setConnectButtonEnabled(boolean isEnable);
        void setDisconnectButtonEnabled(boolean isEnable);

        /**
         * show client money on view
         * @param money trader money
         */
        void showClientMoney(int money);

        /**
         * show client products on view
         * @param products trader products to show
         */
        void showClientProducts(Map<ProductType, Integer> products);

        /**
         * show stock products on view
         * @param products stock products to show
         */
        void showStockProducts(Map<ProductType, Integer> products);

        /**
         * show toast
         * @param message message to show
         */
        void showToastMessage(String message);

        /**
         * remove all info about trader from views
         */
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
