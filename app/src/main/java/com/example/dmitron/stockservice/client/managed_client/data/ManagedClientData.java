package com.example.dmitron.stockservice.client.managed_client.data;

import com.example.dmitron.stockservice.client.Trader;

public class ManagedClientData {

    private static ManagedClientData mInstance;
    private Trader mTrader;
    private UpdateCallback mListener;

    public Trader getTrader() {
        return mTrader;
    }

    private ManagedClientData(){

    }

    public static ManagedClientData getInstance() {
        if (mInstance == null){
            mInstance = new ManagedClientData();
        }

        return mInstance;
    }

    public void notifyListener(){
        mListener.onTraderDataUpdate();
    }

    public void setListener(UpdateCallback listener) {
        this.mListener = listener;
    }

    public void setTrader(Trader trader) {
        this.mTrader = trader;
        mListener.onTraderDataUpdate();
    }

    public interface UpdateCallback{
        void onTraderDataUpdate();
    }
}
