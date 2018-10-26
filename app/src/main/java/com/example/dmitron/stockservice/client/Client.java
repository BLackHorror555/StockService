package com.example.dmitron.stockservice.client;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class Client  {

    protected static final String TAG = "ClientAbstract";
    protected static final int SERVER_PORT = 1234;
    protected static final String ADDRESS = "localhost";

    Socket mSocket;

    public Client() {

    }


    /**
     * connect to server with default port and address
     * @throws IOException failed connection
     */
    public void connectToServer() throws IOException {

        mSocket = new Socket(ADDRESS, SERVER_PORT);

        Log.i(TAG, "connectToServer: yes");


    }


    /**
     * connect to server with specified port and address
     * @throws IOException failed connection
     */
    public void connectToServer(String address, int port) throws IOException {

        mSocket = new Socket(address, port);
        Log.i(TAG, "connectToServer: yes");
    }


    /**
     * close mSocket connection
     * @throws IOException mSocket closing error
     */
    public void closeConnection() throws IOException {
        if (mSocket != null && !mSocket.isClosed())
            mSocket.close();
    }


}