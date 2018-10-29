package com.example.dmitron.stockservice.server_managing.data;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Observable;

/**
 * local storage of server data to display on views
 */
public class ServerManagingLocalData extends Observable implements Serializable {

    public enum Changed{
        CLIENT_COUNT, PRODUCTS, SERVICE_WORKS
    }

    private transient int mConnectedClients;
    private static ServerManagingLocalData sInstance = new ServerManagingLocalData();
    private JSONObject mJsonProducts;

    private ServerManagingLocalData(){
        mConnectedClients = 0;
    }

    /**
     * return instance of local data
     * @return instance
     */
    public static ServerManagingLocalData getInstance(){
        return sInstance;
    }

    /**
     * get client number connected to server
     * @return client count
     */
    public int getClientsCount() {
        return mConnectedClients;
    }

    public void setConnectedClients(int connectedClients) {
        synchronized (this){
            this.mConnectedClients = connectedClients;
            notifyAll(Changed.CLIENT_COUNT);
        }
    }

    public JSONObject getProducts() {
        return mJsonProducts;
    }

    public void setProducts(JSONObject products) {
        synchronized (this) {
            this.mJsonProducts = products;
            notifyAll(Changed.PRODUCTS);
        }
    }

    private void notifyAll(Changed changed){
        setChanged();
        notifyObservers(changed);
    }

}
