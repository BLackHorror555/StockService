package com.example.dmitron.stockservice.client.managed_client.data;

import com.example.dmitron.stockservice.client.Trader;

public class ManagedClientData {

    private static ManagedClientData mInstance;
    private Trader mTrader;
    private UpdateCallback mListener;

    public Trader getTrader() {
        return mTrader;
    }

    public static ManagedClientData getInstance() {
        if (mInstance == null){
            mInstance = new ManagedClientData();
        }

        return mInstance;
    }

    /**
     * notify listener about data changes
     */
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

    /**
     * implement this to get update data calls
     */
    public interface UpdateCallback{
        /**
         * called when trader data updates
         */
        void onTraderDataUpdate();
    }
}
