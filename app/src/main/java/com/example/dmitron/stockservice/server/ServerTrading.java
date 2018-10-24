package com.example.dmitron.stockservice.server;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.stock.ProductType;
import com.example.dmitron.stockservice.stock.StockManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ServerTrading {
    private static final String TAG = "ServerTrading";

    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    private Socket socket;
    private static Context context;

    private static StockManager sm;

    ServerTrading(Socket socket, Context context) {
        this.socket = socket;
        ServerTrading.context = context;

        if (sm == null)
            sm = StockManager.getInstance();

        sendBroadcastUpdateProducts(context);

    }

    /**
     * Send JSON string to mainActivity throw broadcast
     */
    public static void sendBroadcastUpdateProducts(Context context) {
        if (sm == null)
            sm = StockManager.getInstance();

        Intent local = new Intent();
        local.setAction(context.getString(R.string.update_products_action));
        local.putExtra("products", sm.createJson().toString());

        LocalBroadcastManager.getInstance(context).sendBroadcast(local);
    }

    /**
     * initialised input and output socket streams
     * @throws IOException initialisation error
     */
    public void initStreams() throws IOException {
        if (socket != null) {
            dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }
    }

    /**
     * receive requests from client; stop when client send CLOSE_CONNECTION request
     * format of received data:
     * 1 byte - request type
     * 2 & 3 - message length
     * "message length" - message
     *
     * @throws IOException          socket read/write exceptions
     * @throws InterruptedException while waiting for input bytes
     */
    public void start() throws IOException, InterruptedException {

        boolean isRespond = true;

        while (isRespond) {


            //wait until dataIn stream available 3 bytes (1 - request type, 2 - length)
            while (dataIn.available() < 3)
                Thread.sleep(50);

            Log.i(TAG, "start: new request has been discovered");

            RequestType requestType = RequestType.values()[dataIn.readByte()];
            short messageLength = dataIn.readShort();

            //wait until dataIn stream available "length" bytes
            while (true) {
                if (dataIn.available() >= messageLength)
                    break;
            }
            //response to request
            switch (requestType) {
                case PRODUCT_INFO:
                    Log.i(TAG, "serverReceive: RECEIVED NEW PRODUCT REQUEST!");
                    sendProductInfo();
                    break;
                case BUYING:
                    Log.i(TAG, "start: RECEIVED NEW BUYING REQUEST!");
                    ProductType productType = ProductType.values()[dataIn.readByte()];
                    sendBuyingConfirm(productType);
                    break;
                case SELLING:
                    Log.i(TAG, "start: RECEIVED NEW SELLING REQUEST!");
                    ProductType sellingProduct = ProductType.values()[dataIn.readByte()];
                    sendSellingConfirm(sellingProduct);
                    break;
                case CLOSE_CONNECTION:
                    Log.i(TAG, "start: ClientBot want to close connection");
                    isRespond = false;
                    break;
            }
        }
    }


    /**
     * check if certain product is available for sale and send it info to client in 1 byte
     *  and send product price in 4 bytes
     * @param productType type of purchased product
     * @throws IOException write to stream exception
     */
    private void sendBuyingConfirm(ProductType productType) throws IOException {

        int buyingResult = sm.buyProduct(productType);
        if (buyingResult == -1)
            dataOut.writeByte(RequestType.OPERATION_PROHIBITED.ordinal());
        else {
            sendBroadcastUpdateProducts(context);

            dataOut.writeByte(RequestType.OPERATION_ACCEPTED.ordinal());
            dataOut.writeInt(buyingResult);
            dataOut.flush();
        }
    }

    /**
     * send to client selling confirmation and sold item price
     *
     * @param productType type of product need to confirm
     * @throws IOException write to socket
     */
    private void sendSellingConfirm(ProductType productType) throws IOException {

        int sellingPrice = sm.sellProduct(productType);
        sendBroadcastUpdateProducts(context);
        dataOut.writeByte(RequestType.OPERATION_ACCEPTED.ordinal());
        dataOut.writeInt(sellingPrice);
        dataOut.flush();
    }

    /**
     * Creates json with products ("product name" : "product price:)
     * and send it to client
     */
    private void sendProductInfo() throws IOException {

        JSONObject jsonObject;
        jsonObject = sm.createJson();

        //sending json
        dataOut.writeByte(RequestType.PRODUCT_INFO.ordinal());
        int length = jsonObject.toString().getBytes().length;
        dataOut.writeShort(length);
        Log.i(TAG, "sendProductInfo: message product info consist of " + length);

        dataOut.writeBytes(jsonObject.toString());

        dataOut.flush();
    }
}
