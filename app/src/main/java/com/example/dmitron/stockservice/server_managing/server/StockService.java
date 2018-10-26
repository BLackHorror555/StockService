package com.example.dmitron.stockservice.server_managing.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server_managing.data.stock.StockManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StockService extends Service implements StockManager.StockChangesListener {

    private static final String TAG = "StockService";
    private static final int SERVER_PORT = 1234;

    private static boolean sIsWorking = false;
    private int mClientCount;

    private ServerSocket mServerSocket;

    private StockBinder mStockBinder = new StockBinder();

    @Override
    public void onCreate() {
        mClientCount = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        StockManager.getInstance().addListener(this);
        new Thread(new SocketServerThread()).start();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onProductsChanged() {
        sendBroadcastUpdateProducts();
    }

    /**
     * Send JSON string to mainActivity throw broadcast
     */
    private void sendBroadcastUpdateProducts(){
        Intent local = new Intent();
        local.setAction(getString(R.string.update_products_action));
        local.putExtra(getString(R.string.products_extra), StockManager.getInstance().createJson().toString());

        LocalBroadcastManager.getInstance(this).sendBroadcast(local);
    }


    /**
     * Run thread that started socket server and accept clients
     */
    private class SocketServerThread implements Runnable{

        private ThreadPoolExecutor clientsExecutor;

        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket(SERVER_PORT);
                Log.i(TAG, "Start server on port: " + Integer.toString(SERVER_PORT));
                sIsWorking = true;

                sendBroadcastUpdateProducts();

                clientsExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

                while (sIsWorking){
                    Log.i(TAG, "Waiting for a client...");
                    Socket socket = mServerSocket.accept();
                    Log.i(TAG, "Connection accepted");

                    //(new Thread(new ClientHandler(socket))).start();
                    clientsExecutor.submit(new ClientHandler(socket));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    mServerSocket.close();
                    clientsExecutor.shutdownNow();
                    Log.i(TAG, "run: Shutting down server thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Handle client socket and process communication
     */
    private class ClientHandler implements Runnable{



        private Socket socket;

        ClientHandler(Socket socket){
            this.socket = socket;
            mClientCount++;
            sendBroadcastClientCount();
        }

        /**
         * Sends broadcast with new number of clients
         */
        void sendBroadcastClientCount() {
            Intent local = new Intent();

            local.setAction(getString(R.string.client_count_action));
            local.putExtra("client_count", mClientCount);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(local);
        }

        @Override
        public void run() {
            Log.i(TAG, "New client communicate with server!");
            try {

                ServerTrading serverTrading = new ServerTrading(socket, getApplicationContext());
                serverTrading.initStreams();

                serverTrading.start();


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();

            } finally {
                try {
                    mClientCount--;
                    socket.close();
                    sendBroadcastClientCount();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class StockBinder extends Binder {
        public StockService getService(){
            return StockService.this;
        }
    }

    public int getClientCount() {
        return mClientCount;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsWorking = false;
        try {
            mServerSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onDestroy: Shutting down service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: new binding");
        return mStockBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: unbinding");
        return super.onUnbind(intent);
    }

    public static boolean isWorking() {
        return sIsWorking;
    }
}