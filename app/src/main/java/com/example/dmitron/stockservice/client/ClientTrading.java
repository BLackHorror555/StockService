package com.example.dmitron.stockservice.client;

import android.util.Log;

import com.example.dmitron.stockservice.server_managing.server.RequestType;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

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

    private Socket mSocket;

    private DataInputStream mIn;
    private DataOutputStream mOut;

    public ClientTrading(Client client) throws IOException {
        this.mSocket = client.mSocket;
        //trader = new Trader();
        mIn = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
        mOut = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
    }


    /**
     * send to server and activity message that client finished communication
     */
    public void finishConnection() {
        try {
            mOut.writeByte(RequestType.CLOSE_CONNECTION.ordinal());
            mOut.writeShort(0);
            mOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * does request to server to get products
     *
     * @return map of products (type : cost) or null if error
     */
    public Map<ProductType, Integer> getMapProductsFromServer() {

        try {
            mOut.writeByte(RequestType.PRODUCT_INFO.ordinal());
            mOut.writeShort(0);
            mOut.flush();


            Map<ProductType, Integer> products = new EnumMap<>(ProductType.class);
            JSONObject jsonObject = receiveProductInfo();
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                products.put(ProductType.valueOf(key), jsonObject.getInt(key));
            }

            return products;

        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public JSONObject getJsonProductsFromServer() {
        try {
            mOut.writeByte(RequestType.PRODUCT_INFO.ordinal());
            mOut.writeShort(0);
            mOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiveProductInfo();
    }

    /**
     * receive json with products from server
     *
     * @return
     */
    private JSONObject receiveProductInfo() {

        JSONObject jsonObject;
        try {
            //wait until dataIn stream available 3 bytes (1 - request type, 2 - length)
            while (mIn.available() < 3) {
                Thread.sleep(50);
            }

            RequestType requestType = RequestType.values()[mIn.readByte()];
            short messageLength = mIn.readShort();

            //wait until dataIn stream available "length" bytes
            while (true) {
                if (mIn.available() >= messageLength)
                    break;
            }
            Log.i(TAG, "receiveProductInfo: product info message is available to read");
            byte[] bytes = new byte[messageLength];
            mIn.read(bytes, 0, messageLength);
            String jsonString = new String(bytes);

            jsonObject = new JSONObject(jsonString);


            return jsonObject;

        } catch (IOException | JSONException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
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
            Map<ProductType, Integer> products = getMapProductsFromServer();
            if (products == null || products.get(productType) > trader.getMoney())
                return false;

            mOut.writeByte(RequestType.BUYING.ordinal());
            mOut.writeShort(1);
            mOut.writeByte(productType.ordinal());
            mOut.flush();
            int spendMoney = receiveBuyingConfirm();
            if (spendMoney != -1) {
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

            mOut.writeByte(RequestType.SELLING.ordinal());
            mOut.writeShort(1);
            mOut.writeByte(productType.ordinal());
            mOut.flush();

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
        while (mIn.available() < 1)
            Thread.sleep(50);
        RequestType requestType = RequestType.values()[mIn.readByte()];

        while (mIn.available() < 4)
            Thread.sleep(50);

        return mIn.readInt();
    }


    /**
     * do purchase on server and return purchase price or -1 if buying prohibited
     *
     * @return -1 if buying prohibited, otherwise price
     * @throws InterruptedException while waiting for available mIn
     * @throws IOException          error reading from input stream
     */
    private int receiveBuyingConfirm() throws InterruptedException, IOException {
        int price = -1;
        while (mIn.available() < 1)
            Thread.sleep(50);
        RequestType requestType = RequestType.values()[mIn.readByte()];

        if (requestType == RequestType.OPERATION_ACCEPTED) {
            while (mIn.available() < 4)
                Thread.sleep(50);
            price = mIn.readInt();
        }

        return price;
    }
}