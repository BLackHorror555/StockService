package com.example.dmitron.stockservice.server_managing.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.example.dmitron.stockservice.server_managing.server.messages.BuyingMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.ProductMessage;
import com.example.dmitron.stockservice.server_managing.server.messages.SellingMessage;

public class Network {
    public static final int SERVER_PORT = 1234;
    public static final String ADDRESS = "localhost";

    /**
     * register objects at client or server that send over the network
     * @param endPoint client or server
     */
    public static void register(EndPoint endPoint){
        Kryo kryo = endPoint.getKryo();
        kryo.register(BuyingMessage.class);
        kryo.register(SellingMessage.class);
        kryo.register(ProductMessage.class);
    }
}
