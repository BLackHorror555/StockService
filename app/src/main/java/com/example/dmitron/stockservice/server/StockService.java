package com.example.dmitron.stockservice.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.dmitron.stockservice.R;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StockService extends Service {

    private static final String TAG = "StockService";
    private static final int SERVER_PORT = 1234;

    private boolean isListening;
    private int clientCount;

    private ServerSocket serverSocket;

    private StockBinder stockBinder = new StockBinder();

    @Override
    public void onCreate() {
        clientCount = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new SocketServerThread()).start();

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Run thread that started socket server and accept clients
     */
    private class SocketServerThread implements Runnable{

        private ThreadPoolExecutor clientsExecutor;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Log.i(TAG, "Start server on port: " + Integer.toString(SERVER_PORT));
                isListening = true;

                ServerTrading.sendBroadcastUpdateProducts(getApplicationContext());

                clientsExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

                while (isListening){
                    Log.i(TAG, "Waiting for a client...");
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "Connection accepted");

                    //(new Thread(new ClientHandler(socket))).start();
                    clientsExecutor.submit(new ClientHandler(socket));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    serverSocket.close();
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
            clientCount++;
            sendBroadcastClientCount();
        }

        /**
         * Sends broadcast with new number of clients
         */
        void sendBroadcastClientCount() {
            Intent local = new Intent();

            local.setAction(getString(R.string.client_count_action));
            local.putExtra("client_count", Integer.toString(clientCount));

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
                    clientCount--;
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
        return clientCount;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isListening = false;
        try {
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onDestroy: Shutting down service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: new binding");
        return stockBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: unbinding");
        return super.onUnbind(intent);
    }


}