package com.example.dmitron.stockservice.server_managing;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServerManagingContract {

    interface View extends BaseView<Presenter>{
        /**
         * show number of connected clients of views
         * @param clientCount number of clients
         */
        void showConnectedClientsCount(int clientCount);

        /**
         * show updated products on views
         * @param products new products
         * @throws JSONException error parsing JSON
         */
        void updateProductsInfo(JSONObject products) throws JSONException;

        /**
         * set button enabling
         * @param isEnabled is enabled
         */
        void setStartButtonEnabling(boolean isEnabled);
        void setStopButtonEnabling(boolean isEnabled);

        /**
         * show certain product on graph view
         * @param productType product type
         */
        void showProductAtGraph(ProductType productType);

        /**
         * show toast message
         * @param msg message to show
         */
        void showToastMessage(String msg);
    }

    interface Presenter extends BasePresenter{
        /**
         * start background service
         */
        void startService();

        /**
         * stop background service
         */
        void stopService();

        /**
         * called when user select product to monitor changes
         * @param productType product type
         */
        void productToMonitoringSelected(ProductType productType);

        /**
         * called when fragment detached
         */
        void detached();

        /**
         * called when fragment view created
         */
        void viewCreated();
    }
}
