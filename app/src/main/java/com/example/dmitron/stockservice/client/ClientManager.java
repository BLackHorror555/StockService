package com.example.dmitron.stockservice.client;

import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientManager {

    private ClientManager(Handler handler){
        clients = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        mainHandler = handler;
    }

    private static ClientManager clientManager;
    //private Context context;

    private ThreadPoolExecutor clients;
    private Handler mainHandler;


    public static ClientManager getInstance(Handler handler) {
        if (clientManager == null)
            clientManager = new ClientManager(handler);
        return clientManager;
    }

    public void newBotClient(){
        clients.execute(new Client(mainHandler));
    }

    public void newManagedClient(){

    }
}
