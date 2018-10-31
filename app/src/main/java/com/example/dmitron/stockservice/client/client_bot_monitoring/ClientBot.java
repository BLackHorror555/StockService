package com.example.dmitron.stockservice.client.client_bot_monitoring;

import android.util.Log;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.server_managing.server.Network;
import com.example.dmitron.stockservice.server_managing.server.messages.BuyingMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.ProductMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.SellingMessage;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

public class ClientBot implements Runnable {

    private static final String TAG = "ClientBot";
    /**
     * time between deals of each treader in ms
     */
    private static final int TIME_BETWEEN_DEALS = 1000;
    private final int dealsCount;

    private TraderUpdateCallback mListener;
    private Client mClient;
    private Trader mTrader;

    private Listener mServerListener = new Listener.ThreadedListener(new Listener() {
        @Override
        public void received(Connection connection, Object object) {

            if (object instanceof ProductMessage) {
                ProductMessage productMessage = (ProductMessage) object;
                try {
                    buyOrSellGood(productMessage.getMapProducts());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "received: product list received");
            }
            if (object instanceof SellingMessage) {
                SellingMessage sellingMessage = (SellingMessage) object;
                mTrader.pickupProduct(sellingMessage.getProductType());
                mTrader.increaseMoney(sellingMessage.getPrice());
                notifyTraderUpdate();
                Log.i(TAG, "startBotTrading: Bot sell product " + sellingMessage.getProductType().name()
                        + ". Money: " + mTrader.getMoney());
            } else if (object instanceof BuyingMessage) {
                BuyingMessage buyingMessage = (BuyingMessage) object;
                if (buyingMessage.isSuccess()) {
                    mTrader.spendMoney(buyingMessage.getPrice());
                    mTrader.addProduct(buyingMessage.getProductType());
                    notifyTraderUpdate();
                    Log.i(TAG, "startBotTrading: Bot buy product " + buyingMessage.getProductType().name()
                            + ". Money: " + mTrader.getMoney());
                }
            }
        }

        @Override
        public void disconnected(Connection connection) {
            if (mListener != null) {
                mListener.onTraderFinish(mTrader);
            }
        }
    });

    public ClientBot(TraderUpdateCallback listener, int dealsCount) {
        this.mListener = listener;
        this.dealsCount = dealsCount;
    }

    /**
     * send requests on buying or sale goods based on product prices
     *
     * @param products products from server
     */
    private void buyOrSellGood(Map<ProductType, Integer> products) {
        int profitOnBuying;
        int profitOnSale;
        int averageStockProductPrice = 0;
        //find the cheapest product in stock
        ProductType cheapestProduct = null;
        for (Map.Entry<ProductType, Integer> entry : products.entrySet()) {
            if (cheapestProduct == null || entry.getValue() < products.get(cheapestProduct)) {
                cheapestProduct = entry.getKey();
            }
            averageStockProductPrice += entry.getValue();
        }
        averageStockProductPrice /= products.size();
        profitOnBuying = averageStockProductPrice - products.get(cheapestProduct);

        //find among the available trader goods the most expensive for sale
        ProductType expensiveTraderProduct = null;
        for (Map.Entry<ProductType, Integer> entry : mTrader.getProducts().entrySet()) {
            if (expensiveTraderProduct == null || products.get(entry.getKey()) > products.get(expensiveTraderProduct)) {
                expensiveTraderProduct = entry.getKey();
            }
        }
        profitOnSale = products.get(expensiveTraderProduct) - averageStockProductPrice;
        //sell request to buy or sell good
        if ((profitOnBuying > profitOnSale | expensiveTraderProduct == null)
                && mTrader.getMoney() >= products.get(cheapestProduct)) {
            BuyingMessage buyingMessage = new BuyingMessage();
            buyingMessage.setProductType(cheapestProduct);
            buyingMessage.setPrice(mTrader.getMoney());
            mClient.sendTCP(buyingMessage);
            Log.i(TAG, "buyOrSellGood: sell request send");
        } else if (expensiveTraderProduct != null) {
            SellingMessage sellingMessage = new SellingMessage();
            sellingMessage.setProductType(expensiveTraderProduct);
            mClient.sendTCP(sellingMessage);
            Log.i(TAG, "buyOrSellGood: buy request send");
        } else {
            Log.i(TAG, "buyOrSellGood: Bot idle");
        }
    }

    @Override
    public void run() {

        try {
            mClient = new Client();
            mClient.start();
            mClient.connect(5000, Network.ADDRESS, Network.SERVER_PORT);
            mListener.onConnected(true);
            Log.i(TAG, "run: Bot connected to server");
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onConnected(false);
            return;
        }
        Network.register(mClient);
        mClient.addListener(new Listener.ThreadedListener(mServerListener));

        mTrader = new Trader();
        startBotTrading(dealsCount);
        notifyTraderUpdate();
        Log.i(TAG, "startBotTrading: Bot stop trading with money: " + mTrader.getMoney()
                + "and products number - " + mTrader.getProducts().size());
        mClient.stop();
        mClient.close();
        Log.i(TAG, "run: client mSocket closed");

    }

    public void setTraderUpdateListener(TraderUpdateCallback listener) {
        this.mListener = listener;
    }


    /**
     * notify mListener that mTrader data changed
     */
    private void notifyTraderUpdate() {
        if (mListener != null)
            mListener.onTraderUpdate(mTrader);
    }

    /**
     * trading bot, buy and sell products depending on profit (stupid)
     *
     * @param dealsCount how many deals bot do
     * @throws InterruptedException maybe throws when sleep between deals
     */
    private void startBotTrading(int dealsCount) {

        for (int i = 0; i < dealsCount; i++) {
            mClient.sendTCP(new ProductMessage());

            try {
                Thread.sleep(TIME_BETWEEN_DEALS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * implement this to know about mTrader bots updates
     */
    public interface TraderUpdateCallback {
        /**
         * called when client connected
         * @param isSuccess success of connection
         */
        void onConnected(boolean isSuccess);

        /**
         * called when trader data updates
         * @param trader trader with new data
         */
        void onTraderUpdate(Trader trader);

        /**
         * called when bot finishes trading
         * @param trader trader who finished
         */
        void onTraderFinish(Trader trader);
    }
}