package com.example.dmitron.stockservice.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ManagedTraderFragment extends Fragment implements View.OnClickListener, ManagedTraderHelper.ManagedTraderCallback {

    private BroadcastReceiver mUpdateStockProductReceiver;

    private Button mCreateTraderButton;
    private Button mKillTraderButton;
    private TextView mMoneyView;
    private ListView mStockProductsListView;
    private ListView mTraderProductsListView;

    private ArrayAdapter<String> mStockProductsAdapter;
    private ArrayAdapter<String> mTraderProductsAdapter;

    private Trader mTrader;
    private ManagedTraderHelper mManagedTraderHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setProductUpdateReceiver(context);
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

        initViews();

        mCreateTraderButton.setOnClickListener(this);
        mKillTraderButton.setOnClickListener(this);
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
            ProductType productType = ProductType.valueOf(productName);

            mManagedTraderHelper.new BuyingTask(productType).execute();
        });

        mTraderProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mTraderProductsListView.setAdapter(mTraderProductsAdapter);

        mTraderProductsListView.setOnItemClickListener((parent, view, position, id) -> {

            String productName = ((String) parent.getItemAtPosition(position)).split(",")[0];
            ProductType productType = ProductType.valueOf(productName);
            mManagedTraderHelper.new SellingTask(productType).execute();
        });
    }

    /**
     * update adapter and money view with changed mTrader data
     */
    private void updateTraderInfoOnUi() {
        Map<ProductType, Integer> products = mTrader.getProducts();
        mTraderProductsAdapter.clear();

        for (ProductType productType : products.keySet()) {
            mTraderProductsAdapter.add(productType.name() + ", amount - " + products.get(productType));
        }

        mMoneyView.setText(String.format(Locale.getDefault(), "%d", mTrader.getMoney()));
    }


    /**
     * receive info about products from server through broadcast (ugly hack in case of dividing client and server apps)
     *
     * @param context activity context
     */
    private void setProductUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.update_products_action));

        mUpdateStockProductReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                try {
                    JSONObject jsonProducts = new JSONObject(intent.getStringExtra("products"));
                    updateProductsInfo(jsonProducts);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(mUpdateStockProductReceiver, filter);
    }

    /**
     * update list view adapter with new data
     * @param jsonProducts new products
     * @throws JSONException json read error
     */
    private void updateProductsInfo(JSONObject jsonProducts) throws JSONException {
        Iterator<String> iterator = jsonProducts.keys();
        mStockProductsAdapter.clear();

        while (iterator.hasNext()) {
            String product = iterator.next();
            mStockProductsAdapter.add(product + ", price - " + jsonProducts.getInt(product));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateStockProductReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_trader_button:
                createManagedTrader();
                break;

            case R.id.kill_trader_button:
                mManagedTraderHelper.new FinishConnectionTask().execute();
                break;
        }
    }

    /**
     * create new managed mTrader and connect to server
     * is success return in callback
     */
    private void createManagedTrader() {
        mTrader = new Trader();

        mManagedTraderHelper = new ManagedTraderHelper(this, mTrader);
        mManagedTraderHelper.new ConnectToServerTask().execute();

    }

    @Override
    public void onBuyingCompleted(boolean success) {
        if (!success)
            Toast.makeText(getActivity(), "Not enough money!", Toast.LENGTH_SHORT).show();
        else
            updateTraderInfoOnUi();
    }

    @Override
    public void onSellingCompleted(boolean success) {
        if (!success)
            Toast.makeText(getActivity(), "No such product", Toast.LENGTH_SHORT).show();
        else
            updateTraderInfoOnUi();

    }

    @Override
    public void onConnectionTaskComplete(boolean success) {
        if (!success) {
            Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();

        } else {
            updateTraderInfoOnUi();
            mCreateTraderButton.setEnabled(false);
            mKillTraderButton.setEnabled(true);
        }

    }

    @Override
    public void onConnectionFinishTaskComplete() {
        mTraderProductsAdapter.clear();
        mKillTraderButton.setEnabled(false);
        mMoneyView.setText("");
        mCreateTraderButton.setEnabled(true);
    }
}
