package com.example.dmitron.stockservice.client.client_bot_monitoring;

import com.example.dmitron.stockservice.BasePresenter;
import com.example.dmitron.stockservice.BaseView;
import com.example.dmitron.stockservice.client.Trader;

public interface ClientBotMonitoringContract {

    interface View extends BaseView<Presenter>{
        void showTraderInfo(Trader trader);
        void showTraderDisconnected(int traderId);
        void showTraderOnGraph(String traderId);
    }

    interface Presenter extends BasePresenter{
        void newBotClient();
        void clientSelected(String id);
    }
}
