package com.example.dmitron.stockservice.client.client_bot_monitoring;

import android.util.Log;

import com.example.dmitron.stockservice.client.Client;
import com.example.dmitron.stockservice.client.ClientTrading;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.io.IOException;
import java.util.Map;

public class ClientBot extends Client implements Runnable{

    private static final String TAG = "ClientBot";


    /**
     * time between deals of each treader in ms
     */
    private static final int TIME_BETWEEN_DEALS = 1000;


    private ClientTrading mClientTrading;
    private TraderUpdateCallback mListener;

    private Trader mTrader;

    public ClientBot(TraderUpdateCallback listener) {
        this.mListener = listener;
    }


    @Override
    public void run() {
        try {
            connectToServer();
            mTrader = new Trader();
            mListener.onConnected(true);
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onConnected(false);
            return;
        }
        try{
            mClientTrading = new ClientTrading(this);

            startBotTrading(10);

            if (mListener != null){
                mListener.onTraderFinish(mTrader);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection();
                Log.i(TAG, "run: client mSocket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTraderUpdateListener(TraderUpdateCallback listener){
        this.mListener = listener;
    }


    /**
     * notify mListener that mTrader data changed
     */
    private void notifyTraderUpdate(){
        if (mListener != null)
            mListener.onTraderUpdate(mTrader);
    }


    /**
     * trading bot, buy and sell products depending on profit (stupid)
     *
     * @param dealsCount how many deals bot do
     * @throws InterruptedException maybe throws when sleep between deals
     */
    private void startBotTrading(int dealsCount) throws InterruptedException {

        try {
            for (int i = 0; i < dealsCount; i++) {

                Map<ProductType, Integer> products =  mClientTrading.getProductsFromServer();

                ProductType cheapestProduct = null;
                //profit on buying
                for (Map.Entry<ProductType, Integer> entry : products.entrySet()) {

                    if (cheapestProduct == null || entry.getValue() < products.get(cheapestProduct)) {
                        cheapestProduct = entry.getKey();
                    }

                }

                //buy cheapest good if has money
                if (mTrader.getMoney() >= products.get(cheapestProduct) && i < 7) {
                    mClientTrading.buyProduct(mTrader, cheapestProduct);
                    Log.i(TAG, "startBotTrading: Bot buy product; remaining money - " + mTrader.getMoney());
                    notifyTraderUpdate();
                }
                //else sell the most expensive
                else {
                    ProductType expensiveProduct = null;
                    //profit on buying
                    for (Map.Entry<ProductType, Integer> entry : mTrader.getProducts().entrySet()) {

                        if (expensiveProduct == null || products.get(entry.getKey()) > products.get(expensiveProduct)) {
                            expensiveProduct = entry.getKey();
                        }

                    }
                    mClientTrading.sellProduct(mTrader, expensiveProduct);
                    Log.i(TAG, "startBotTrading: Bot sell product for " + products.get(expensiveProduct)
                            + ". Money: " + mTrader.getMoney());
                    notifyTraderUpdate();
                }

                Thread.sleep(TIME_BETWEEN_DEALS);
            }

        }
        finally {
            mClientTrading.finishConnection();
            Log.i(TAG, "startBotTrading: Bot stop trading with money: " + mTrader.getMoney());
        }

    }

    /**
     * implement this to know about mTrader bots updates
     */
    public interface TraderUpdateCallback {
        void onConnected(boolean isSuccess);
        void onTraderUpdate(Trader trader);
        void onTraderFinish(Trader trader);
    }

}