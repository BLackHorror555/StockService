package com.example.dmitron.stockservice.client.managed_client;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;

import java.util.Locale;
import java.util.Map;

public class ManagedTraderFragment extends Fragment implements View.OnClickListener,
        ManagedClientContract.View {

    private Button mCreateTraderButton;
    private Button mKillTraderButton;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private TextView mMoneyView;
    private ListView mStockProductsListView;
    private ListView mTraderProductsListView;

    private ArrayAdapter<String> mStockProductsAdapter;
    private ArrayAdapter<String> mTraderProductsAdapter;

    private ManagedClientContract.Presenter mPresenter;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        new ManagedClientPresenter(this, context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_managed_trader, container, false);

        mCreateTraderButton = v.findViewById(R.id.create_trader_button);
        mMoneyView = v.findViewById(R.id.trader_money_view);
        mStockProductsListView = v.findViewById(R.id.stock_products_list);
        mTraderProductsListView = v.findViewById(R.id.trader_products_list_view);
        mKillTraderButton = v.findViewById(R.id.kill_trader_button);
        mConnectButton = v.findViewById(R.id.connect_managed_client_button);
        mDisconnectButton = v.findViewById(R.id.disconnect_managed_client_button);

        initViews();

        mCreateTraderButton.setOnClickListener(this);
        mKillTraderButton.setOnClickListener(this);
        mConnectButton.setOnClickListener(this);
        mDisconnectButton.setOnClickListener(this);

        mPresenter.viewCreated();
        return v;
    }

    /**
     * initialise product and mTrader list views
     */
    private void initViews() {
        mStockProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mStockProductsListView.setAdapter(mStockProductsAdapter);

        mStockProductsListView.setOnItemClickListener((parent, view, position, id) -> {

            String productName = ((String) parent.getItemAtPosition(position)).split(",")[0];
            mPresenter.stockProductTapped(ProductType.valueOf(productName));
        });

        mTraderProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mTraderProductsListView.setAdapter(mTraderProductsAdapter);

        mTraderProductsListView.setOnItemClickListener((parent, view, position, id) -> {

            String productName = ((String) parent.getItemAtPosition(position)).split(",")[0];
            mPresenter.traderProductTapped(ProductType.valueOf(productName));
        });
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mStockProductsAdapter.clear();
        mTraderProductsAdapter.clear();
        mPresenter = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_trader_button:
                mPresenter.createManagedClient();
                break;
            case R.id.kill_trader_button:
                mPresenter.killManagedClient();
                break;
            case R.id.connect_managed_client_button:
                mPresenter.connect();
                break;
            case R.id.disconnect_managed_client_button:
                mPresenter.disconnect();
                break;
        }
    }


    @Override
    public void setPresenter(ManagedClientContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setCreateTraderButtonEnabled(boolean isEnable) {
        mCreateTraderButton.setEnabled(isEnable);
    }

    @Override
    public void setKillTraderButtonEnabled(boolean isEnable) {
        mKillTraderButton.setEnabled(isEnable);
    }

    @Override
    public void setConnectButtonEnabled(boolean isEnable) {
        mConnectButton.setEnabled(isEnable);
    }

    @Override
    public void setDisconnectButtonEnabled(boolean isEnable) {
        mDisconnectButton.setEnabled(isEnable);
    }

    @Override
    public void showClientMoney(int money) {
        mMoneyView.setText(String.format(Locale.getDefault(), "%d", money));
    }

    @Override
    public void showClientProducts(Map<ProductType, Integer> products) {

        mTraderProductsAdapter.clear();
        if (products == null) return;
        for (ProductType productType : products.keySet()) {
            mTraderProductsAdapter.add(productType.name() + ", amount - " + products.get(productType));
        }
    }

    @Override
    public void showStockProducts(Map<ProductType, Integer> products) {


        mStockProductsAdapter.clear();
        if (products == null) return;
        for (ProductType type : products.keySet()){

            mStockProductsAdapter.add( type.name() + ", price - " + products.get(type));
        }
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void cleanTraderInfo() {
        showClientProducts(null);
        showClientMoney(0);
    }
}