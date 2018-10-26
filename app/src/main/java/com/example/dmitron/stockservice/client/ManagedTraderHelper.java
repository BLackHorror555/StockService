package com.example.dmitron.stockservice.client;

import android.os.AsyncTask;

import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;

import java.io.IOException;

public class ManagedTraderHelper {

    private ManagedTraderCallback mListener;
    private ClientTrading mClientTrading;
    private final Trader mTrader;
    private Client mClient;


    public ManagedTraderHelper(ManagedTraderCallback listener, final Trader trader) {
        this.mListener = listener;
        this.mTrader = trader;
    }

    /**
     * connects to the server in asynchronous way
     */
    public class ConnectToServerTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                mClient = new Client();
                mClient.connectToServer();
                mClientTrading = new ClientTrading(mClient);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean success) {
            mListener.onConnectionTaskComplete(success);
        }
    }


    /**
     * do buying request to server in asynchronous way
     */
    public class BuyingTask extends AsyncTask<Void, Void, Boolean>{


        ProductType productType;

        public BuyingTask(ProductType productType){
            this.productType = productType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mClientTrading.buyProduct(mTrader, productType);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mListener.onBuyingCompleted(success);
        }
    }

    /**
     * do selling request to server in asynchronous way
     */
    public class SellingTask extends AsyncTask<Void, Void, Boolean>{


        ProductType productType;

        public SellingTask(ProductType productType){
            this.productType = productType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mClientTrading.sellProduct(mTrader, productType);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mListener.onSellingCompleted(success);
        }
    }

    /**
     * notify server about finishing connection in asynchronous way
     */
    public class FinishConnectionTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                mClientTrading.finishConnection();
                mClient.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mListener.onConnectionFinishTaskComplete();
        }
    }


    /**
     * implement this to receive callbacks about completed tasks
     */
    public interface ManagedTraderCallback{
        void onBuyingCompleted(boolean success);
        void onSellingCompleted(boolean success);
        void onConnectionTaskComplete(boolean success);
        void onConnectionFinishTaskComplete();
    }
}
