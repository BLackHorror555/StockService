package com.example.dmitron.stockservice.client.managed_client;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.client.managed_client.data.ManagedClientData;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.util.Map;


public class ManagedClientPresenter implements ManagedClientContract.Presenter, ManagedTraderHelper.ManagedTraderCallback, ManagedClientData.UpdateCallback {

    private static final String TAG = "ManagedClientPresenter";

    private ManagedClientData mClientData;
    private ManagedTraderHelper mManagedTraderHelper;
    private ManagedClientContract.View mView;
    private Context mContext;
    private Handler mMainHandler;

    ManagedClientPresenter(ManagedClientContract.View view, Context context) {
        mContext = context;
        mView = view;
        mClientData = ManagedClientData.getInstance();
        mClientData.setListener(this);
        mMainHandler = new Handler(Looper.getMainLooper());
        view.setPresenter(this);
    }

    @Override
    public void viewCreated() {
    }

    @Override
    public void connect() {
        if(mManagedTraderHelper == null) {
            mManagedTraderHelper = new ManagedTraderHelper(this);
        }
        if (mClientData.getTrader() == null) {
            mView.showToastMessage(mContext.getString(R.string.trader_does_not_created_msg));
        } else {
            mManagedTraderHelper.new ConnectToServerTask().execute();
        }
    }

    @Override
    public void disconnect() {
        mManagedTraderHelper.stopMakingProductRequests();
        mManagedTraderHelper.new FinishConnectionTask().execute();
    }

    @Override
    public void onConnectionTaskComplete(boolean success) {
        if (!success) {
            mView.showToastMessage(mContext.getString(R.string.connection_failed_msg));
        } else {
            mManagedTraderHelper.startMakingProductRequests();
            mView.setCreateTraderButtonEnabled(false);
            mView.setKillTraderButtonEnabled(true);
            mView.setConnectButtonEnabled(false);
            mView.setDisconnectButtonEnabled(true);
        }
    }

    @Override
    public void onConnectionFinishTaskComplete() {
        mView.setKillTraderButtonEnabled(true);
        mView.setCreateTraderButtonEnabled(true);
        mView.setConnectButtonEnabled(true);
        mView.setDisconnectButtonEnabled(false);
    }

    @Override
    public void onProductsRequestComplete(Map<ProductType, Integer> products) {
        mView.showStockProducts(products);
    }

    @Override
    public void onServerDisconnected() {
        mMainHandler.post(() -> {
            mView.showToastMessage("Server disconnected");
            mView.setConnectButtonEnabled(true);
            mView.setDisconnectButtonEnabled(false);
        });
    }

    @Override
    public void createManagedClient() {
        Trader trader = new Trader();
        mClientData.setTrader(trader);
    }

    @Override
    public void traderProductTapped(ProductType productType) {
        mManagedTraderHelper.new SellingTask(productType).execute();
    }

    @Override
    public void stockProductTapped(ProductType productType) {
        mManagedTraderHelper.new BuyingTask(productType).execute();
    }

    @Override
    public void killManagedClient() {
        disconnect();
        mView.setKillTraderButtonEnabled(false);
        mClientData.setTrader(null);
        mView.showClientProducts(null);
        mView.showClientMoney(0);
    }


    @Override
    public void onTraderDataUpdate() {
        mMainHandler.post(() -> {
            if (mClientData.getTrader() == null){
                mView.cleanTraderInfo();
                return;
            }
            mView.showClientMoney(mClientData.getTrader().getMoney());
            mView.showClientProducts(mClientData.getTrader().getProducts());
        });
    }

    @Override
    public void start() {
    }
}
