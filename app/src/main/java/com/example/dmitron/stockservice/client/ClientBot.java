package com.example.dmitron.stockservice.client;

import android.os.Handler;
import android.util.Log;

import com.example.dmitron.stockservice.stock.ProductType;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ClientBot implements Runnable {

    private static final String TAG = "Client1";
    private static final int SERVER_PORT = 1234;
    private static final String ADDRESS = "localhost";

    /**
     * time between deals of each treader in ms
     */
    private static final int TIME_BETWEEN_DEALS = 1000;

    private Socket socket;
    private ClientTrading clientTrading;
    private Handler mainHandler;

    private Trader trader;

    public ClientBot(Handler handler) {
        mainHandler = handler;
        trader = new Trader();
    }


    @Override
    public void run() {
        try {
            connectToServer();
            clientTrading = new ClientTrading(socket, mainHandler);
            clientTrading.initStreams();
            startBotTrading(10);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
                Log.i(TAG, "run: client socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                }

                Thread.sleep(TIME_BETWEEN_DEALS);
            }

        }
        finally {
            clientTrading.finishConnection(trader);
            Log.i(TAG, "startBotTrading: Bot stop trading with money: " + trader.getMoney());
        }

    }



    private void connectToServer() throws IOException {

        socket = new Socket(ADDRESS, SERVER_PORT);
        Log.i(TAG, "connectToServer: yes");
    }
}