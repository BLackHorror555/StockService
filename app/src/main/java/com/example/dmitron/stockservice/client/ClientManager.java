package com.example.dmitron.stockservice.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientManager {

    private ClientManager(){
        clients = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    private static ClientManager clientManager;
    //private Context context;

    private ThreadPoolExecutor clients;


    public static ClientManager getInstance() {
        if (clientManager == null)
            clientManager = new ClientManager();
        return clientManager;
    }

    public void newClient(){
        clients.submit(new Client());
    }
}
