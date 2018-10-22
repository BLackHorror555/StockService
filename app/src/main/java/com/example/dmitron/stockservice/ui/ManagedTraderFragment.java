package com.example.dmitron.stockservice.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.client.Trader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

public class ManagedTraderFragment extends Fragment implements View.OnClickListener {

    private BroadcastReceiver updateStockProductReceiver;

    private Button createTraderButton;
    private TextView moneyView;
    private TextView stockProductsView;
    private ListView stockProductsListView;

    private ArrayAdapter<String> stockProductsAdapter;

    private Trader trader;

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
        stockProductsView = v.findViewById(R.id.stock_products_view);

        initListView();

        createTraderButton.setOnClickListener(this);
        return v;
    }

    private void initListView() {
        stockProductsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        stockProductsListView.setAdapter(stockProductsAdapter);

        stockProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void setProductUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.update_products_receiver));

        updateStockProductReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (getString(R.string.update_products_receiver).equals(intent.getAction())) {
                    stockProductsView.setText("");

                    try {
                        JSONObject products = new JSONObject(intent.getStringExtra("products"));

                        Iterator<String> iterator = products.keys();
                        while (iterator.hasNext()) {
                            String product = iterator.next();
                            //update products view
                            stockProductsView.append(product + " : " + products.get(product) + "\n");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        context.registerReceiver(updateStockProductReceiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(updateStockProductReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.create_trader_button:
                trader = new Trader();
                moneyView.setText(String.format(Locale.getDefault(), "%d", trader.getMoney()));
                createTraderButton.setEnabled(false);
                break;
        }
    }
}
