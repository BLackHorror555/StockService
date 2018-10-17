package com.example.dmitron.stockservice.Client;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable {

    private static final String TAG = "Client1";
    private static final int SERVER_PORT = 1234;
    private static final String ADDRESS = "localhost";

    private Socket socket;
    private ClientTrading clientTrading;

    public Client() {
    }

    @Override
    public void run() {
        try {
            connectToServer();
            clientTrading = new ClientTrading(socket);
            clientTrading.initStreams();
            clientTrading.startBotTrading(10);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
                Log.i(TAG, "run: client socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void connectToServer() throws IOException {

        socket = new Socket(ADDRESS, SERVER_PORT);
        Log.i(TAG, "connectToServer: yes");
    }
}