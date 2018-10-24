package com.example.dmitron.stockservice.client;

import android.util.Log;

import com.example.dmitron.stockservice.stock.ProductType;

import java.io.IOException;
import java.util.Map;

public class ClientBot extends Client implements Runnable{

    private static final String TAG = "ClientBot";


    /**
     * time between deals of each treader in ms
     */
    private static final int TIME_BETWEEN_DEALS = 1000;


    private ClientTrading clientTrading;
    private TraderUpdateCallback listener;

    private Trader trader;

    public ClientBot(TraderUpdateCallback listener) {
        this.listener = listener;
        trader = new Trader();
    }


    @Override
    public void run() {
        try {
            connectToServer();
            clientTrading = new ClientTrading(this);

            startBotTrading(10);

            if (listener != null){
                listener.onTradingFinish(trader);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection();
                Log.i(TAG, "run: client socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTraderUpdateListener(TraderUpdateCallback listener){
        this.listener = listener;
    }


    private void notifyTraderUpdate(){
        if (listener != null)
            listener.onTraderUpdate(trader);
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

                Map<ProductType, Integer> products =  clientTrading.getProductsFromServer();

                ProductType cheapestProduct = null;
                //profit on buying
                for (Map.Entry<ProductType, Integer> entry : products.entrySet()) {

                    if (cheapestProduct == null || entry.getValue() < products.get(cheapestProduct)) {
                        cheapestProduct = entry.getKey();
                    }

                }

                //buy cheapest good if has money
                if (trader.getMoney() >= products.get(cheapestProduct) && i < 7) {
                    clientTrading.buyProduct(trader, cheapestProduct);
                    Log.i(TAG, "startBotTrading: Bot buy product; remaining money - " + trader.getMoney());
                    notifyTraderUpdate();
                }
                //else sell the most expensive
                else {
                    ProductType expensiveProduct = null;
                    //profit on buying
                    for (Map.Entry<ProductType, Integer> entry : trader.getProducts().entrySet()) {

                        if (expensiveProduct == null || products.get(entry.getKey()) > products.get(expensiveProduct)) {
                            expensiveProduct = entry.getKey();
                        }

                    }
                    clientTrading.sellProduct(trader, expensiveProduct);
                    Log.i(TAG, "startBotTrading: Bot sell product for " + products.get(expensiveProduct)
                            + ". Money: " + trader.getMoney());
                    notifyTraderUpdate();
                }

                Thread.sleep(TIME_BETWEEN_DEALS);
            }

        }
        finally {
            clientTrading.finishConnection();
            Log.i(TAG, "startBotTrading: Bot stop trading with money: " + trader.getMoney());
        }

    }

    /**
     * implement this to know about trader bots updates
     */
    public interface TraderUpdateCallback {
        void onTraderUpdate(Trader trader);
        void onTradingFinish(Trader trader);
    }

}