package com.example.dmitron.stockservice.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientBotManager {

    private ClientBotManager(){
        clientsPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }

    private static ClientBotManager clientManager;
    private ThreadPoolExecutor clientsPool;


    public static ClientBotManager getInstance() {
        if (clientManager == null)
            clientManager = new ClientBotManager();
        return clientManager;
    }

    /**
     * create new bot trader
     * @param listener
     */
    public void newClientBot(ClientBot.TraderUpdateCallback listener){
        clientsPool.execute(new ClientBot(listener));
    }


    /**
     * cancel all client bots
     */
    public void cancelAllClients(){
        clientsPool.shutdownNow();
    }
}
