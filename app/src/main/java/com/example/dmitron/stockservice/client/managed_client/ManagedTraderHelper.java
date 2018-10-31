package com.example.dmitron.stockservice.client.managed_client;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.example.dmitron.stockservice.client.managed_client.data.ManagedClientData;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.server_managing.server.Network;
import com.example.dmitron.stockservice.server_managing.server.messages.BuyingMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.ProductMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.SellingMessage;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;


public class ManagedTraderHelper {

    private static final String TAG = "ManagedTraderHelper";
    /**
     * number of updates of products from server per second
     */
    private static final int PRODUCTS_UPDATE_FREQUENCY = 2;
    private ManagedTraderCallback mListener;
    private Client mClient;
    private Handler mHandler;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    ManagedTraderHelper(ManagedTraderCallback listener) {
        this.mListener = listener;
    }

    /**
     * start making product requests to server
     */
    void startMakingProductRequests() {

        HandlerThread mHandlerThread = new HandlerThread("RequestThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ProductMessage productMessage = new ProductMessage();
                synchronized (mClient) {
                    mClient.sendTCP(productMessage);
                }
                mHandler.postDelayed(this, 1000 / PRODUCTS_UPDATE_FREQUENCY);
            }
        });
    }

    /**
     * stop make products requests to server
     */
    void stopMakingProductRequests() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * implement this to receive callbacks about completed tasks
     */
    public interface ManagedTraderCallback {

        /**
         * called when connection async task completed
         * @param success is connection success
         */
        void onConnectionTaskComplete(boolean success);

        /**
         * called when finish connection async task completed
         */
        void onConnectionFinishTaskComplete();

        /**
         * called when product request to server completed
         * @param products products received from server
         */
        void onProductsRequestComplete(Map<ProductType, Integer> products);

        /**
         * called when server itself terminate the connection
         */
        void onServerDisconnected();
    }

    /**
     * connects to the server in asynchronous way
     */
    public class ConnectToServerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                mClient = new Client();
                mClient.start();
                mClient.connect(5000, Network.ADDRESS, Network.SERVER_PORT);

                Network.register(mClient);

                mClient.addListener(new Listener.ThreadedListener(new Listener() {
                    @Override
                    public void received(Connection connection, Object object) {

                        if (object instanceof ProductMessage) {
                            ProductMessage productMessage = (ProductMessage) object;
                            mMainHandler.post(() -> {
                                try {
                                    mListener.onProductsRequestComplete(productMessage.getMapProducts());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        if (object instanceof SellingMessage) {
                            SellingMessage sellingMessage = (SellingMessage) object;
                            ManagedClientData.getInstance().getTrader().pickupProduct(sellingMessage.getProductType());
                            ManagedClientData.getInstance().getTrader().increaseMoney(sellingMessage.getPrice());
                            ManagedClientData.getInstance().notifyListener();
                            Log.i(TAG, "received: Product has sold");

                        } else if (object instanceof BuyingMessage) {
                            BuyingMessage buyingMessage = (BuyingMessage) object;
                            if (buyingMessage.isSuccess()) {
                                ManagedClientData.getInstance().getTrader().addProduct(buyingMessage.getProductType());
                                ManagedClientData.getInstance().getTrader().spendMoney(buyingMessage.getPrice());
                                ManagedClientData.getInstance().notifyListener();
                                Log.i(TAG, "received: Product was bought");
                            }
                        }
                    }

                    @Override
                    public void disconnected(Connection connection) {
                        mListener.onServerDisconnected();
                    }
                }));

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
    public class BuyingTask extends AsyncTask<Void, Void, Void> {


        ProductType productType;

        public BuyingTask(ProductType productType) {
            this.productType = productType;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            BuyingMessage buyingMessage = new BuyingMessage();
            buyingMessage.setProductType(productType);
            buyingMessage.setPrice(ManagedClientData.getInstance().getTrader().getMoney());

            synchronized (mClient) {
                mClient.sendTCP(buyingMessage);
            }
            return null;
        }

    }

    /**
     * do selling request to server in asynchronous way
     */
    public class SellingTask extends AsyncTask<Void, Void, Boolean> {


        ProductType productType;

        public SellingTask(ProductType productType) {
            this.productType = productType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            SellingMessage sellingMessage = new SellingMessage();
            sellingMessage.setProductType(productType);

            synchronized (mClient) {
                mClient.sendTCP(sellingMessage);
            }
            return null;
        }


    }

    public class FinishConnectionTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... voids) {


            mClient.stop();
            mClient.close();
            return true;

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mListener.onConnectionFinishTaskComplete();
            }
        }


    }

}



