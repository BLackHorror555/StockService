package com.example.dmitron.stockservice.client;

import android.content.Context;
import android.os.Message;
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

    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private Context AppContext;

    private android.os.Handler mainHandler;


    ClientTrading(Socket socket, android.os.Handler handler) throws IOException {
        this.socket = socket;
        //trader = new Trader();
        mainHandler = handler;
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }


    void initStreams() throws IOException {
        if (socket != null) {

        }
    }

    /**
     * send json with info about trader to main handler:
     *  id - int
     *  money - int
     *  products - json[
     *  product name - count
     *        ...
     *  ]
     *
     */
    private void updateClientInfo(boolean isCancel, Trader trader){
        try {
            JSONObject jsonClient = new JSONObject()
                    .put("id", Integer.toString(trader.getID()))
                    .put("money", trader.getMoney())
                    .put("isCancel", isCancel);

            JSONObject jsonProducts = new JSONObject();
            for (Map.Entry<ProductType, Integer> entry : trader.getProducts().entrySet()){
                jsonProducts.put(entry.getKey().name(), entry.getValue());
            }
            jsonClient.put("products", jsonProducts);

            Message msg = mainHandler.obtainMessage();
            msg.obj = jsonClient;
            msg.sendToTarget();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * send to server and activity message that client finished communication
     */
    void finishConnection(Trader trader) {
        try {
            out.writeByte(RequestType.CLOSE_CONNECTION.ordinal());
            out.writeShort(0);
            out.flush();

            updateClientInfo(true, trader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update map products by request to server and receiving data
     */
    Map<ProductType, Integer> getProductsFromServer() {
        try {
            out.writeByte(RequestType.PRODUCT_INFO.ordinal());
            //Log.i(TAG, "getProductsFromServer: sent " + RequestType.PRODUCT_INFO.ordinal() + "ordinal");
            out.writeShort(0);
            //Log.i(TAG, "getProductsFromServer: product request has been sent");
            out.flush();
            //this.products = receiveProductInfo();
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
    boolean buyProduct(Trader trader, ProductType productType) {
        boolean success = false;
        try {
            out.writeByte(RequestType.BUYING.ordinal());
            out.writeShort(1);
            out.writeByte(productType.ordinal());
            out.flush();
            int spendMoney = receiveBuyingConfirm();
            if (spendMoney != -1){
                trader.spendMoney(spendMoney);
                trader.addProduct(productType);
                success = true;
                updateClientInfo(false, trader);
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
    boolean sellProduct(Trader trader, ProductType productType) {
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

            updateClientInfo(false, trader);

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
     * @return
     * @throws InterruptedException
     * @throws IOException
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