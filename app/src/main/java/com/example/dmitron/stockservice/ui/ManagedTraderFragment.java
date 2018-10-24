package com.example.dmitron.stockservice.ui;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.client.ManagedTraderHelper;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.stock.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ManagedTraderFragment extends Fragment implements View.OnClickListener, ManagedTraderHelper.ManagedTraderCallback {

    private BroadcastReceiver updateStockProductReceiver;

    private Button createTraderButton;
    private Button killTraderButton;
    private TextView moneyView;
    private ListView stockProductsListView;
    private ListView traderProductsListView;

    private ArrayAdapter<String> stockProductsAdapter;
    private ArrayAdapter<String> traderProductsAdapter;

    private Trader trader;
    private ManagedTraderHelper managedTraderHelper;

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

        createTraderButton = v.findViewById(R.id.create_trader_button);
        moneyView = v.findViewById(R.id.trader_money_view);
        stockProductsListView = v.findViewById(R.id.stock_products_list);
        traderProductsListView = v.findViewById(R.id.trader_products_list_view);
        killTraderButton = v.findViewById(R.id.kill_trader_button);

        initViews();

        createTraderButton.setOnClickListener(this);
        killTraderButton.setOnClickListener(this);
        return v;
    }

    /**
     * initialise product and trader list views
     */
    private void initViews() {
        stockProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        stockProductsListView.setAdapter(stockProductsAdapter);

        stockProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String productName = ((String) parent.getItemAtPosition(position)).split(",")[0];
                ProductType productType = ProductType.valueOf(productName);

                managedTraderHelper.new BuyingTask(productType).execute();


            }
        });

        traderProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        traderProductsListView.setAdapter(traderProductsAdapter);

        traderProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String productName = ((String) parent.getItemAtPosition(position)).split(",")[0];
                ProductType productType = ProductType.valueOf(productName);

                managedTraderHelper.new SellingTask(productType).execute();
            }
        });
    }

    /**
     * update adapter and money view with changed trader data
     */
    private void updateTraderInfoOnUi() {
        Map<ProductType, Integer> products = trader.getProducts();
        traderProductsAdapter.clear();

        for (ProductType productType : products.keySet()) {
            traderProductsAdapter.add(productType.name() + ", amount - " + products.get(productType));
        }

        moneyView.setText(String.format(Locale.getDefault(), "%d", trader.getMoney()));
    }

    /**
     * receive info about products from server through broadcast (ugly hack in case of dividing client and server apps)
     *
     * @param context activity context
     */
    private void setProductUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.update_products_action));

        updateStockProductReceiver = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(context).registerReceiver(updateStockProductReceiver, filter);
    }

    /**
     * update list view adapter with new data
     * @param jsonProducts new products
     * @throws JSONException json read error
     */
    private void updateProductsInfo(JSONObject jsonProducts) throws JSONException {
        Iterator<String> iterator = jsonProducts.keys();
        stockProductsAdapter.clear();

        while (iterator.hasNext()) {
            String product = iterator.next();
            stockProductsAdapter.add(product + ", price - " + jsonProducts.getInt(product));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateStockProductReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_trader_button:
                createManagedTrader();
                break;

            case R.id.kill_trader_button:
                managedTraderHelper.new FinishConnectionTask().execute();
                break;
        }
    }

    /**
     * create new managed trader and connect to server
     * is success return in callback
     */
    private void createManagedTrader() {
        trader = new Trader();

        managedTraderHelper = new ManagedTraderHelper(this, trader);
        managedTraderHelper.new ConnectToServerTask().execute();

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
            createTraderButton.setEnabled(false);
            killTraderButton.setEnabled(true);
        }

    }

    @Override
    public void onConnectionFinishTaskComplete() {
        traderProductsAdapter.clear();
        killTraderButton.setEnabled(false);
        moneyView.setText("");
        createTraderButton.setEnabled(true);
    }
}
