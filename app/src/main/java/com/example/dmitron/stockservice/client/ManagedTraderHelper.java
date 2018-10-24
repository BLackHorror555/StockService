package com.example.dmitron.stockservice.client;

import android.os.AsyncTask;

import com.example.dmitron.stockservice.stock.ProductType;

import java.io.IOException;

public class ManagedTraderHelper {

    private ManagedTraderCallback listener;
    private ClientTrading clientTrading;
    private final Trader trader;
    private Client client;


    public ManagedTraderHelper(ManagedTraderCallback listener, final Trader trader) {
        this.listener = listener;
        this.trader = trader;
    }

    /**
     * connects to the server in asynchronous way
     */
    public class ConnectToServerTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                client = new Client();
                client.connectToServer();
                clientTrading = new ClientTrading(client);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean success) {
            listener.onConnectionTaskComplete(success);
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
            return clientTrading.buyProduct(trader, productType);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            listener.onBuyingCompleted(success);
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
            return clientTrading.sellProduct(trader, productType);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            listener.onSellingCompleted(success);
        }
    }

    /**
     * notify server about finishing connection in asynchronous way
     */
    public class FinishConnectionTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                clientTrading.finishConnection();
                client.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.onConnectionFinishTaskComplete();
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
