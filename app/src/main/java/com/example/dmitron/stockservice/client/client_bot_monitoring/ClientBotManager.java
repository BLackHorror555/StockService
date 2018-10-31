package com.example.dmitron.stockservice.client.client_bot_monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientBotManager {
    private static final int DEFAULT_DEALS_COUNT = 15;

    private static ClientBotManager sClientBotManager;
    private ThreadPoolExecutor mClientsPool;

    private ClientBotManager(){
        mClientsPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }

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
        mClientsPool.execute(new ClientBot(listener, DEFAULT_DEALS_COUNT));
    }

    /**
     * cancel all client bots
     */
    public void cancelAllClients(){
        mClientsPool.shutdownNow();
    }
}
