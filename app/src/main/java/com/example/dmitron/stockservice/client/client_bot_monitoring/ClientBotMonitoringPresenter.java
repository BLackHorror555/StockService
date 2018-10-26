package com.example.dmitron.stockservice.client.client_bot_monitoring;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.dmitron.stockservice.client.Trader;

public class ClientBotMonitoringPresenter implements ClientBotMonitoringContract.Presenter, ClientBot.TraderUpdateCallback {

    private Context mContext;
    private ClientBotMonitoringContract.View mView;
    private Handler handler;

    ClientBotMonitoringPresenter(ClientBotMonitoringContract.View view, Context context){
        mContext = context;
        mView = view;
        view.setPresenter(this);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void start() {

    }

    @Override
    public void newBotClient() {
        ClientBotManager.getInstance().newClientBot(this);
    }

    @Override
    public void clientSelected(String id) {
        mView.showTraderOnGraph(id);
    }


    @Override
    public void onConnected(boolean isSuccess) {
        if (!isSuccess) {
            handler.post(() -> Toast.makeText(mContext, "Failed connection", Toast.LENGTH_SHORT).show());
        }

    }

    @Override
    public void onTraderUpdate(final Trader trader) {
        handler.post(() -> {
            mView.showTraderInfo(trader);
        });

    }

    @Override
    public void onTraderFinish(final Trader trader) {

        handler.post(() -> {
            mView.showTraderDisconnected(trader.getId());

        });

    }
}
