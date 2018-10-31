package com.example.dmitron.stockservice.server_managing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server_managing.data.ServerManagingLocalData;
import com.example.dmitron.stockservice.server_managing.data.ServerManagingLocalData.Changed;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.server_managing.server.StockService;
import com.example.dmitron.stockservice.utils.ActivityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

public class ServerManagingPresenter implements ServerManagingContract.Presenter, Observer {

    private ServerManagingContract.View mView;
    private Context mContext;

    private ServerManagingLocalData mLocalData;
    private boolean isServiceWorks;

    /**
     * receive broadcasts from service with client count
     */
    private BroadcastReceiver mClientCountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLocalData.setConnectedClients(intent.getIntExtra(context.getString(R.string.client_count_extra), 0));
        }
    };

    /**
     * receive broadcasts from service with product updates
     */
    private BroadcastReceiver mUpdateProductsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject jsonProducts = new JSONObject(intent.getStringExtra(context.getString(R.string.products_extra)));
                mLocalData.setProducts(jsonProducts);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    ServerManagingPresenter(@NonNull ServerManagingContract.View view, Context context) {
        this.mView = view;
        this.mContext = context;
        mLocalData = ServerManagingLocalData.getInstance();
        view.setPresenter(this);
        registerReceivers();
    }

    private void restorePreviousSettings() {
        SharedPreferences sharedPref =
                mContext.getSharedPreferences(mContext.getString(R.string.preference_file_service_info), Context.MODE_PRIVATE);
        try {
            mLocalData.setProducts(new JSONObject(sharedPref.getString(mContext.getString(R.string.saved_products), "")));
            mView.showToastMessage("Last service data restored");
        } catch (JSONException e) {
            e.printStackTrace();
            mView.showToastMessage("Unable to restore data");
        }
    }

    /**
     * Register receivers to receive intents with a new number of clients and new products
     */
    void registerReceivers() {
        mLocalData.addObserver(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(mContext.getString(R.string.client_count_action));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mClientCountReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(mContext.getString(R.string.update_products_action));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdateProductsReceiver, filter);
    }

    @Override
    public void viewCreated() {
        if (StockService.isWorking()) {
            mView.setStartButtonEnabling(false);
            mView.setStopButtonEnabling(true);

            restorePreviousSettings();
        }
    }

    void unregisterReceivers() {
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
        isServiceWorks = true;
    }

    @Override
    public void stopService() {
        mContext.stopService(new Intent(mContext, StockService.class));
        mView.setStartButtonEnabling(true);
        mView.setStopButtonEnabling(false);
        isServiceWorks = false;
    }

    @Override
    public void productToMonitoringSelected(ProductType productType) {
        mView.showProductAtGraph(productType);
    }

    @Override
    public void detached() {
        unregisterReceivers();
        SharedPreferences.Editor editor =
                ActivityUtils.getSharedPrefEditor(mContext, mContext.getString(R.string.preference_file_service_info));
        editor.putString(mContext.getString(R.string.saved_products), mLocalData.getProducts().toString()).commit();
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((arg == Changed.CLIENT_COUNT)) {
            mView.showConnectedClientsCount(mLocalData.getClientsCount());
        } else {
            try {
                mView.updateProductsInfo(mLocalData.getProducts());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
