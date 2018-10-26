package com.example.dmitron.stockservice.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientBotManager {

    private ClientBotManager(){
        mClientsPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }

    private static ClientBotManager sClientBotManager;
    private ThreadPoolExecutor mClientsPool;


    public static ClientBotManager getInstance() {
        if (sClientBotManager == null)
            sClientBotManager = new ClientBotManager();
        return sClientBotManager;
    }

    /**
     * create new bot trader
     * @param listener
     */
    public void newClientBot(ClientBot.TraderUpdateCallback listener){
        mClientsPool.execute(new ClientBot(listener));
    }


    /**
     * cancel all client bots
     */
    public void cancelAllClients(){
        mClientsPool.shutdownNow();
    }
}
