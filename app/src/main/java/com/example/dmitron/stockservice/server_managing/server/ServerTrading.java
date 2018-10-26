package com.example.dmitron.stockservice.server_managing.server;

import android.content.Context;
import android.util.Log;

import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.server_managing.data.stock.StockManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ServerTrading {
    private static final String TAG = "ServerTrading";

    private DataInputStream mDataIn;
    private DataOutputStream mDataOut;

    private Socket mSocket;

    private static StockManager sStockManager;

    ServerTrading(Socket socket, Context context) {
        this.mSocket = socket;

        if (sStockManager == null)
            sStockManager = StockManager.getInstance();

    }


    /**
     * initialised input and output mSocket streams
     * @throws IOException initialisation error
     */
    public void initStreams() throws IOException {
        if (mSocket != null) {
            mDataIn = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
            mDataOut = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
        }
    }

    /**
     * receive requests from client; stop when client send CLOSE_CONNECTION request
     * format of received data:
     * 1 byte - request type
     * 2 & 3 - message length
     * "message length" - message
     *
     * @throws IOException          mSocket read/write exceptions
     * @throws InterruptedException while waiting for input bytes
     */
    public void start() throws IOException, InterruptedException {

        boolean isRespond = true;

        while (isRespond) {


            //wait until mDataIn stream available 3 bytes (1 - request type, 2 - length)
            while (mDataIn.available() < 3)
                Thread.sleep(50);

            Log.i(TAG, "start: new request has been discovered");

            RequestType requestType = RequestType.values()[mDataIn.readByte()];
            short messageLength = mDataIn.readShort();

            //wait until mDataIn stream available "length" bytes
            while (true) {
                if (mDataIn.available() >= messageLength)
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
                    ProductType productType = ProductType.values()[mDataIn.readByte()];
                    sendBuyingConfirm(productType);
                    break;
                case SELLING:
                    Log.i(TAG, "start: RECEIVED NEW SELLING REQUEST!");
                    ProductType sellingProduct = ProductType.values()[mDataIn.readByte()];
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

        int buyingResult = sStockManager.buyProduct(productType);
        if (buyingResult == -1)
            mDataOut.writeByte(RequestType.OPERATION_PROHIBITED.ordinal());
        else {
            mDataOut.writeByte(RequestType.OPERATION_ACCEPTED.ordinal());
            mDataOut.writeInt(buyingResult);
            mDataOut.flush();
        }
    }

    /**
     * send to client selling confirmation and sold item price
     *
     * @param productType type of product need to confirm
     * @throws IOException write to mSocket
     */
    private void sendSellingConfirm(ProductType productType) throws IOException {

        int sellingPrice = sStockManager.sellProduct(productType);
        mDataOut.writeByte(RequestType.OPERATION_ACCEPTED.ordinal());
        mDataOut.writeInt(sellingPrice);
        mDataOut.flush();
    }

    /**
     * Creates json with products ("product name" : "product price:)
     * and send it to client
     */
    private void sendProductInfo() throws IOException {

        JSONObject jsonObject;
        jsonObject = sStockManager.createJson();

        //sending json
        mDataOut.writeByte(RequestType.PRODUCT_INFO.ordinal());
        int length = jsonObject.toString().getBytes().length;
        mDataOut.writeShort(length);
        Log.i(TAG, "sendProductInfo: message product info consist of " + length);

        mDataOut.writeBytes(jsonObject.toString());

        mDataOut.flush();
    }
}
