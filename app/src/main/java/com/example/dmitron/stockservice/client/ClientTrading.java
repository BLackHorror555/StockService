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


/**
 * provides api to do server requests
 */
public class ClientTrading {


    private static final String TAG = "ClientTrading";

    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    public ClientTrading(Client client) throws IOException {
        this.socket = client.socket;
        //trader = new Trader();
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }



    /**
     * send to server and activity message that client finished communication
     */
    public void finishConnection() {
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
    public Map<ProductType, Integer> getProductsFromServer() {
        try {
            out.writeByte(RequestType.PRODUCT_INFO.ordinal());
            out.writeShort(0);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiveProductInfo();
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
     * @param productType product to buy
     * @return success
     */
    public boolean buyProduct(Trader trader, ProductType productType) {
        boolean success = false;
        try {
            if (getProductsFromServer().get(productType) > trader.getMoney())
                return false;

            out.writeByte(RequestType.BUYING.ordinal());
            out.writeShort(1);
            out.writeByte(productType.ordinal());
            out.flush();
            int spendMoney = receiveBuyingConfirm();
            if (spendMoney != -1){
                trader.spendMoney(spendMoney);
                trader.addProduct(productType);
                success = true;
            } else
                Log.i(TAG, "buyProduct: refused to buy");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }


    /**
     * send request for selling product and receive confirmation from server
     *
     * @param productType product for selling
     */
    public boolean sellProduct(Trader trader, ProductType productType) {
        try {
            if (!trader.isHasProduct(productType))
                return false;

            out.writeByte(RequestType.SELLING.ordinal());
            out.writeShort(1);
            out.writeByte(productType.ordinal());
            out.flush();

            int soldItemPrice = receiveSellingConfirmation();

            trader.pickupProduct(productType);
            trader.increaseMoney(soldItemPrice);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * Wait for server to confirm of operation
     *
     * @return sold item price
     * @throws InterruptedException while wait for reading
     * @throws IOException          while checking available symbols
     */
    private int receiveSellingConfirmation() throws InterruptedException, IOException {
        while (in.available() < 1)
            Thread.sleep(50);
        RequestType requestType = RequestType.values()[in.readByte()];

        while (in.available() < 4)
            Thread.sleep(50);

        return in.readInt();
    }


    /**
     * do purchase on server and return purchase price or -1 if buying prohibited
     * @return -1 if buying prohibited, otherwise price
     * @throws InterruptedException while waiting for available in
     * @throws IOException  error reading from input stream
     */
    private int receiveBuyingConfirm() throws InterruptedException, IOException {
        int price = -1;
        while (in.available() < 1)
            Thread.sleep(50);
        RequestType requestType = RequestType.values()[in.readByte()];

        if (requestType == RequestType.OPERATION_ACCEPTED){
            while (in.available() < 4)
                Thread.sleep(50);
            price = in.readInt();
        }

        return price;
    }
}