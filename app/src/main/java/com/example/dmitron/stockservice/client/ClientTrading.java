package com.example.dmitron.stockservice.client;

import android.util.Log;

import com.example.dmitron.stockservice.server.RequestType;
import com.example.dmitron.stockservice.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class ClientTrading {

    private static final String TAG = "ClientTrading";

    /**
     * time between deals of each treader in ms
     */
    private static final int TIME_BETWEEN_DEALS = 1000;

    //for bot clientTrading; set to false to stop
    private boolean isTrading;
    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;


    /**
     * Products on server received from last request
     * Map - Product type : Price
     */
    private Map<ProductType, Integer> products;

    private final Trader trader;

    ClientTrading(Socket socket) {
        this.socket = socket;
        trader = new Trader();
        isTrading = true;
    }


    public void initStreams() throws IOException {
        if (socket != null) {
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }
    }


    /**
     * trading bot, buy and sell products depending on profit (stupid)
     *
     * @param dealsCount how many deals bot do
     * @throws InterruptedException maybe throws when sleep between deals
     */
    public void startBotTrading(int dealsCount) throws InterruptedException {


        for (int i = 0; i < dealsCount; i++) {

            updateProductsFromServer();

            ProductType cheapestProduct = null;
            //profit on buying
            for (Map.Entry<ProductType, Integer> entry : products.entrySet()) {

                if (cheapestProduct == null || entry.getValue() < products.get(cheapestProduct)) {
                    cheapestProduct = entry.getKey();
                }

            }

            //buy cheapest good if has money
            if (trader.getMoney() >= products.get(cheapestProduct) && i < 7) {
                buyProduct(cheapestProduct);
                Log.i(TAG, "startBotTrading: Bot buy product; remaining money - " + trader.getMoney());
            }
            //else sell the most expensive
            else {
                ProductType expensiveProduct = null;
                //profit on buying
                for (Map.Entry<ProductType, Integer> entry : trader.getProducts().entrySet()) {

                    if (expensiveProduct == null || products.get(entry.getKey()) > products.get(expensiveProduct)) {
                        expensiveProduct = entry.getKey();
                    }

                }
                sellProduct(expensiveProduct);
                Log.i(TAG, "startBotTrading: Bot sell product for " + products.get(expensiveProduct)
                        + ". Money: " + trader.getMoney());
            }

            Thread.sleep(TIME_BETWEEN_DEALS);
        }
        sendFinishConnection();
        Log.i(TAG, "startBotTrading: Bot stop trading with money: " + trader.getMoney());


    }

    public void test() {
        updateProductsFromServer();
        buyProduct(ProductType.ORANGE);
        sellProduct(ProductType.ORANGE);
        sendFinishConnection();
    }


    /**
     * send to server message that client finished communication
     */
    private void sendFinishConnection() {
        try {
            out.writeByte(RequestType.CLOSE_CONNECTION.ordinal());
            out.writeShort(0);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update map products by request to server and receiving data
     */
    private Map<ProductType, Integer> updateProductsFromServer() {
        try {
            out.writeByte(RequestType.PRODUCT_INFO.ordinal());
            //Log.i(TAG, "updateProductsFromServer: sent " + RequestType.PRODUCT_INFO.ordinal() + "ordinal");
            out.writeShort(0);
            //Log.i(TAG, "updateProductsFromServer: product request has been sent");
            out.flush();
            this.products = receiveProductInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    private Map<ProductType, Integer> receiveProductInfo() {
        Map<ProductType, Integer> products = new EnumMap<>(ProductType.class);
        try {
            //wait until dataIn stream available 3 bytes (1 - request type, 2 - length)
            while (in.available() < 3)
                Thread.sleep(50);

            RequestType requestType = RequestType.values()[in.readByte()];
            short messageLength = in.readShort();

            //wait until dataIn stream available "length" bytes
            while (true) {
                if (in.available() >= messageLength)
                    break;
            }
            Log.i(TAG, "receiveProductInfo: product info message is available to read");
            byte[] bytes = new byte[messageLength];
            in.read(bytes, 0, messageLength);
            String jsonString = new String(bytes);

            JSONObject jsonObject = new JSONObject(jsonString);

            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                products.put(ProductType.valueOf(key), jsonObject.getInt(key));
            }

        } catch (IOException | JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        return products;
    }


    /**
     * send request for purchasing product and receive confirmation from server
     *
     * @param productType product to by
     */
    private void buyProduct(ProductType productType) {
        try {
            out.writeByte(RequestType.BUYING.ordinal());
            out.writeShort(1);
            out.writeByte(productType.ordinal());
            out.flush();
            if (receiveConfirmation()) {
                trader.spendMoney(products.get(productType));
                trader.addProduct(productType);
            } else
                Log.i(TAG, "buyProduct: refused to buy");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * send request for selling product and receive confirmation from server
     *
     * @param productType product for selling
     */
    private void sellProduct(ProductType productType) {
        try {
            out.writeByte(RequestType.SELLING.ordinal());
            out.writeShort(1);
            out.writeByte(productType.ordinal());
            out.flush();
            if (receiveConfirmation()) {
                trader.increaseMoney(products.get(productType));
                trader.pickupProduct(productType);
            } else
                Log.i(TAG, "sellProduct: refused to sell");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * Wait for server to confirm of operation
     *
     * @return is confirm successful
     * @throws InterruptedException while wait for reading
     * @throws IOException          while checking available symbols
     */
    private boolean receiveConfirmation() throws InterruptedException, IOException {
        while (in.available() < 1)
            Thread.sleep(50);
        RequestType requestType = RequestType.values()[in.readByte()];
        return requestType == RequestType.OPERATION_ACCEPTED;
    }
}