package com.example.dmitron.stockservice.server_managing.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server_managing.data.stock.StockManager;
import com.example.dmitron.stockservice.server_managing.server.messages.BuyingMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.ProductMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.SellingMessage;

import java.io.IOException;

public class StockService extends Service implements StockManager.StockChangesListener {

    private static final String TAG = "StockService";

    private static boolean sIsWorking = false;
    private Server mServer;
    private StockManager mStockManager;
    private int clientCount = 0;

    private Listener mListener = new Listener() {
        @Override
        public void connected(Connection connection) {
            clientCount++;
            sendBroadcastUpdateClientCount();
        }

        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof ProductMessage) {
                ProductMessage productMessage = (ProductMessage) object;
                productMessage.setProductsJson(mStockManager.createJson());
                connection.sendTCP(productMessage);
            }
            if (object instanceof SellingMessage) {
                SellingMessage sellingMessage = (SellingMessage) object;
                sellingMessage.setPrice(mStockManager.sellProduct(sellingMessage.getProductType()));
                sellingMessage.setSuccess(true);
                connection.sendTCP(sellingMessage);
            } else if (object instanceof BuyingMessage) {
                BuyingMessage buyingMessage = (BuyingMessage) object;
                int price = mStockManager.buyProduct(buyingMessage.getProductType(), buyingMessage.getPrice());
                if (price == -1) {
                    buyingMessage.setSuccess(false);
                } else {
                    buyingMessage.setPrice(price);
                    buyingMessage.setSuccess(true);
                }
                connection.sendTCP(buyingMessage);
            }
        }

        @Override
        public void disconnected(Connection connection) {
            clientCount--;
            sendBroadcastUpdateClientCount();
        }

    };

    /**
     * @return is service works
     */
    public static boolean isWorking() {
        return sIsWorking;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mStockManager = StockManager.getInstance();
        mStockManager.addListener(this);

        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startServer() throws IOException {
        mServer = new Server();
        mServer.start();
        mServer.bind(Network.SERVER_PORT);

        Network.register(mServer);

        mServer.addListener(new Listener.ThreadedListener(mListener));
    }

    @Override
    public void onProductsChanged() {
        sendBroadcastUpdateProducts();
    }

    /**
     * Send JSON string to mainActivity throw broadcast
     */
    private void sendBroadcastUpdateProducts() {
        Intent local = new Intent();
        local.setAction(getString(R.string.update_products_action));
        local.putExtra(getString(R.string.products_extra), StockManager.getInstance().createJson().toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(local);
    }

    private void sendBroadcastUpdateClientCount(){
        Intent local = new Intent();
        local.setAction(getString(R.string.client_count_action));
        local.putExtra(getString(R.string.client_count_extra), clientCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(local);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsWorking = false;

        mServer.stop();
        mServer.close();

        Log.i(TAG, "onDestroy: Shutting down service");
    }

}