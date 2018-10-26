package com.example.dmitron.stockservice.servermanaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.servermanaging.data.LocalData;
import com.example.dmitron.stockservice.servermanaging.data.LocalData.Changed;
import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;
import com.example.dmitron.stockservice.servermanaging.server.StockService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class ServerManagingPresenter implements ServerManagingContract.Presenter, Observer {

    private ServerManagingContract.View mView;
    private Context mContext;

    private LocalData mLocalData = LocalData.getInstance();

    private BroadcastReceiver mClientCountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLocalData.setConnectedClients(intent.getIntExtra(context.getString(R.string.client_count_extra), 0));
        }
    };

    private BroadcastReceiver mUpdateProductsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject jsonProducts = new JSONObject(intent.getStringExtra(context.getString(R.string.products_extra)));
                Map<ProductType, Integer> products = new HashMap<>();

                for (Iterator<String> it = jsonProducts.keys(); it.hasNext(); ) {
                    String name = it.next();
                    products.put(ProductType.valueOf(name), jsonProducts.getInt(name));
                }
                mLocalData.setProducts(products);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };


    ServerManagingPresenter(@NonNull ServerManagingContract.View view, Context context){
        this.mView = view;
        this.mContext = context;
        view.setPresenter(this);
        registerReceivers();
    }


    /**
     * Register receivers to receive intents with a new number of clients and new products
     */
    void registerReceivers(){
        mLocalData.addObserver(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(mContext.getString(R.string.client_count_action));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mClientCountReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(mContext.getString(R.string.update_products_action));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdateProductsReceiver, filter);
    }

    void unregisterReceivers(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mClientCountReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateProductsReceiver);
    }


    @Override
    public void start() {
    }

    @Override
    public void startService() {

        Intent intent = new Intent(mContext, StockService.class);
        mContext.startService(intent);
        mView.setStartButtonEnabling(false);
        mView.setStopButtonEnabling(true);
    }

    @Override
    public void stopService() {
        mContext.stopService(new Intent(mContext, StockService.class));
        mView.setStartButtonEnabling(true);
        mView.setStopButtonEnabling(false);
    }

    @Override
    public void productToMonitoringSelected(ProductType productType) {
        mView.showProductAtGraph(productType);
    }

    @Override
    public void detached() {
        unregisterReceivers();
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((arg == Changed.CLIENT_COUNT)){
            mView.showConnectedClientsCount(mLocalData.getClientsCount());
        }
        else{
            mView.updateProductsInfo(mLocalData.getProducts());
        }
    }
}
