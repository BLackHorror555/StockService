package com.example.dmitron.stockservice.client.client_bot_monitoring;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.client.Trader;

public interface ClientBotMonitoringContract {

    interface View extends BaseView<Presenter>{
        /**
         * show new info on views about trader
         * @param trader trader with new data
         */
        void updateTraderInfo(Trader trader);

        /**
         * show on views that trader disconnected
         * @param traderId trader id
         */
        void showTraderDisconnected(int traderId);

        /**
         * show info on views about certain trader
         * @param traderId trader id
         */
        void showCertainTraderInfo(String traderId);
    }

    interface Presenter extends BasePresenter{
        /**
         * create new bot client
         */
        void newBotClient();

        /**
         * called when user select trader in spinner
         * @param id id of selected trader
         */
        void clientSelected(String id);
    }
}
