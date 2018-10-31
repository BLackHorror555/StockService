package com.example.dmitron.stockservice.server_managing.data;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Observable;

/**
 * local storage of server data to display on views
 */
public class ServerManagingLocalData extends Observable implements Serializable {

    private static ServerManagingLocalData sInstance = new ServerManagingLocalData();
    private transient int mConnectedClients;
    private JSONObject mJsonProducts;
    private ServerManagingLocalData() {
        mConnectedClients = 0;
    }

    /**
     * return instance of local data
     *
     * @return instance
     */
    public static ServerManagingLocalData getInstance() {
        return sInstance;
    }

    /**
     * get client number connected to server
     *
     * @return client count
     */
    public int getClientsCount() {
        return mConnectedClients;
    }

    /**
     * set number of connected to server clients
     * @param connectedClients number of clients
     */
    public void setConnectedClients(int connectedClients) {
        synchronized (this) {
            this.mConnectedClients = connectedClients;
            notifyAll(Changed.CLIENT_COUNT);
        }
    }

    /**
     * @return products in JSONObject
     */
    public synchronized JSONObject getProducts() {
        return mJsonProducts;
    }

    /**
     * @param products products in JSON format
     */
    public synchronized void setProducts(JSONObject products) {
        synchronized (this) {
            this.mJsonProducts = products;
            notifyAll(Changed.PRODUCTS);
        }
    }

    /**
     * notify all listeners about data changes
     * @param changed what data has changed
     */
    private synchronized void notifyAll(Changed changed) {
        setChanged();
        notifyObservers(changed);
    }

    /**
     * define what data changed
     */
    public enum Changed {
        CLIENT_COUNT, PRODUCTS, SERVICE_WORKS
    }

}
