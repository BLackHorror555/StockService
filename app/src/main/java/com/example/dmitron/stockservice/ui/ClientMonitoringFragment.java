package com.example.dmitron.stockservice.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.client.ClientBot;
import com.example.dmitron.stockservice.client.ClientBotManager;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.stock.ProductType;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientMonitoringFragment extends Fragment implements View.OnClickListener {

    private GraphView moneyGraph;
    private HashMap<String, LineGraphSeries<DataPoint>> moneySeriesMap;
    private int lastX = 0;

    //UI
    private Button newClientButton;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private TextView productsView;

    //private static Handler handler;

    //utils
    Random rnd;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    /**
     * receive callbacks when whatever client bot update
     */
    private ClientBot.TraderUpdateCallback traderUpdateCallback = new ClientBot.TraderUpdateCallback() {

        Handler handler = new Handler(Looper.getMainLooper());
        @Override
        public void onTraderUpdate(final Trader trader) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateMoneyGraph(Integer.toString(trader.getID()), trader.getMoney());
                    Map<ProductType, Integer> products = trader.getProducts();
                    productsView.setText("");
                    for (ProductType productType : products.keySet()){
                        productsView.append(productType.name() + " - " + products.get(productType) + "\n");
                    }
                }
            });

        }

        @Override
        public void onTradingFinish(final Trader trader) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    String id = Integer.toString(trader.getID());
                    //int pos = adapter.getPosition("Trader " + id);
                    adapter.remove("Trader " + id);
                    adapter.insert("Trader " + id + " - disconnected", adapter.getCount());
                }
            });

        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_client_monitoring, container, false);

        newClientButton = v.findViewById(R.id.new_client_btn);
        moneyGraph =  v.findViewById(R.id.client_money_graph);
        spinner = v.findViewById(R.id.client_spinner);
        productsView = v.findViewById(R.id.products_view);

        newClientButton.setOnClickListener(this);

        rnd = new Random();

        createGraphs();
        initSpinner();
        return v;
    }

    private void initSpinner() {
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                name = name.split(" ")[1];
                moneyGraph.removeAllSeries();
                if (moneySeriesMap.containsKey(name))
                    moneyGraph.addSeries(moneySeriesMap.get(name));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /**
     * put new data points with money into graph
     * @param id
     * @param money
     */
    private void updateMoneyGraph(String id, int money) {

        //add client if he is not
        if (!moneySeriesMap.containsKey(id)) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            series.setTitle("Trader " + id);

            moneySeriesMap.put(id, series);
            adapter.add("Trader " + id);
        }

        DataPoint dataPoint = new DataPoint(lastX, money);
        moneySeriesMap.get(id).appendData(dataPoint, true, 20);
        lastX++;
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void createGraphs() {
        moneyGraph.getViewport().setXAxisBoundsManual(true);
        //moneyGraph.getViewport().setMinX(0);
        moneyGraph.getViewport().setMaxX(40);
        moneyGraph.getViewport().setMinY(0);

        moneyGraph.getViewport().setScalable(true);
        moneyGraph.getViewport().setScrollable(true);
        moneyGraph.getViewport().setScalableY(true);
        moneyGraph.getViewport().setScrollableY(true);

        moneyGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        moneyGraph.setTitle("ClientBot money monitoring");
        moneyGraph.setTitleTextSize(50);

        moneyGraph.getLegendRenderer().setVisible(true);
        moneyGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        moneyGraph.getLegendRenderer().setWidth(350);
        moneyGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

        moneySeriesMap = new HashMap<>();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_client_btn:
                ClientBotManager.getInstance().newClientBot(traderUpdateCallback);
                break;
        }
    }
}
