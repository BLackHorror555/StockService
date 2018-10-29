package com.example.dmitron.stockservice.client.managed_client;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.example.dmitron.stockservice.client.Client;
import com.example.dmitron.stockservice.client.ClientTrading;
import com.example.dmitron.stockservice.client.managed_client.data.ManagedClientData;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.io.IOException;
import java.util.Map;

public class ManagedTraderHelper {
    /**
     * number of updates of products from server per second
     */
    public static final int PRODUCTS_UPDATE_FREQUENCY = 2;


    private ManagedTraderCallback mListener;
    private ClientTrading mClientTrading;
    private Client mClient;
    private Handler mHandler;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * make repetitive requests to server
     */
    private Runnable mRunProductRequest = new Runnable() {
        @Override
        public void run() {

            final Map<ProductType, Integer> productsMap;

            productsMap = requestProduct();
            if (productsMap == null) {
                mMainHandler.post(() -> mListener.onServerDisconnected());
                return;
            }
            mMainHandler.post(() -> mListener.onProductsRequestComplete(productsMap));
            mHandler.postDelayed(this, 1000 / PRODUCTS_UPDATE_FREQUENCY);
        }
    };


    public ManagedTraderHelper(ManagedTraderCallback listener) {
        this.mListener = listener;
    }

    /**
     * start making product requests to server
     */
    public void startMakingProductRequests() {
        HandlerThread mHandlerThread = new HandlerThread("RequestThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mRunProductRequest);
    }

    private Map<ProductType, Integer> requestProduct() {
        synchronized (mClientTrading) {
            return mClientTrading.getMapProductsFromServer();
        }
    }

    public void stopMakingProductRequests() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * connects to the server in asynchronous way
     */
    public class ConnectToServerTask extends AsyncTask<Void, Void, Boolean> {

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
     * makes the request to the server and return products in json
     */
    public class ProductsRequestTask extends AsyncTask<Void, Void, Map<ProductType, Integer>> {

        @Override
        protected Map<ProductType, Integer> doInBackground(Void... voids) {
            synchronized (mClientTrading) {
                return mClientTrading.getMapProductsFromServer();

            }
        }

        @Override
        protected void onPostExecute(Map<ProductType, Integer> products) {
            if (products == null) mListener.onServerDisconnected();
            mListener.onProductsRequestComplete(products);
        }


    }
    /**
     * do buying request to server in asynchronous way
     */
    public class BuyingTask extends AsyncTask<Void, Void, Boolean> {


        ProductType productType;

        public BuyingTask(ProductType productType) {
            this.productType = productType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean returnValue;
            synchronized (mClientTrading) {
                returnValue = mClientTrading.buyProduct(ManagedClientData.getInstance().getTrader(), productType);
            }

            return returnValue;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                ManagedClientData.getInstance().notifyListener();
            }
            mListener.onBuyingCompleted(success);
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

            synchronized (mClientTrading) {
                return mClientTrading.sellProduct(ManagedClientData.getInstance().getTrader(), productType);
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                ManagedClientData.getInstance().notifyListener();
            }
            mListener.onSellingCompleted(success);
        }


    }
    /**
     * notify server about finishing connection in asynchronous way
     */
    public class FinishConnectionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {

                synchronized (mClientTrading) {
                    mClientTrading.finishConnection();
                    mClient.closeConnection();
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mListener.onConnectionFinishTaskComplete();
            }
        }


    }

    /**
     * implement this to receive callbacks about completed tasks
     */
    public interface ManagedTraderCallback {
        void onBuyingCompleted(boolean success);

        void onSellingCompleted(boolean success);

        void onConnectionTaskComplete(boolean success);

        void onConnectionFinishTaskComplete();

        void onProductsRequestComplete(Map<ProductType, Integer> products);

        void onServerDisconnected();
    }

}




