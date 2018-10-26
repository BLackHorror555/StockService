package com.example.dmitron.stockservice.client;

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
import android.widget.Toast;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.servermanaging.data.stock.ProductType;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientMonitoringFragment extends Fragment implements View.OnClickListener {

    private GraphView mMoneyGraph;
    private HashMap<String, LineGraphSeries<DataPoint>> mMoneySeriesMap;
    private int mLastX = 0;

    //UI
    private Button mNewClientButton;
    private Spinner mSpinner;
    private ArrayAdapter<String> mArrayAdapter;
    private TextView mProductsView;

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
        public void onConnected(boolean isSuccess) {
            if (!isSuccess) {
                handler.post(() -> Toast.makeText(getActivity(), "Failed connection", Toast.LENGTH_SHORT).show());
            }

        }

        @Override
        public void onTraderUpdate(final Trader trader) {
            handler.post(() -> {
                updateMoneyGraph(Integer.toString(trader.getId()), trader.getMoney());
                Map<ProductType, Integer> products = trader.getProducts();
                mProductsView.setText("");
                for (ProductType productType : products.keySet()) {
                    mProductsView.append(productType.name() + " - " + products.get(productType) + "\n");
                }
            });

        }

        @Override
        public void onTradingFinish(final Trader trader) {

            handler.post(() -> {
                String id = Integer.toString(trader.getId());
                mArrayAdapter.remove("Trader " + id);
                mArrayAdapter.insert("Trader " + id + " - disconnected", mArrayAdapter.getCount());
            });

        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_client_monitoring, container, false);

        mNewClientButton = v.findViewById(R.id.new_client_btn);
        mMoneyGraph = v.findViewById(R.id.client_money_graph);
        mSpinner = v.findViewById(R.id.client_spinner);
        mProductsView = v.findViewById(R.id.products_view);

        mNewClientButton.setOnClickListener(this);

        rnd = new Random();

        createGraphs();
        initSpinner();
        return v;
    }

    /**
     * initialise mSpinner with mArrayAdapter and item selected list
     */
    private void initSpinner() {
        mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mArrayAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                name = name.split(" ")[1];
                mMoneyGraph.removeAllSeries();
                if (mMoneySeriesMap.containsKey(name))
                    mMoneyGraph.addSeries(mMoneySeriesMap.get(name));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /**
     * put new data points with money into graph
     *
     * @param id
     * @param money
     */
    private void updateMoneyGraph(String id, int money) {

        //add client if he is not
        if (!mMoneySeriesMap.containsKey(id)) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            series.setTitle("Trader " + id);

            mMoneySeriesMap.put(id, series);
            mArrayAdapter.add("Trader " + id);
        }

        DataPoint dataPoint = new DataPoint(mLastX, money);
        mMoneySeriesMap.get(id).appendData(dataPoint, true, 20);
        mLastX++;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * setup graph
     */
    private void createGraphs() {
        mMoneyGraph.getViewport().setXAxisBoundsManual(true);
        //mMoneyGraph.getViewport().setMinX(0);
        mMoneyGraph.getViewport().setMaxX(40);
        mMoneyGraph.getViewport().setMinY(0);

        mMoneyGraph.getViewport().setScalable(true);
        mMoneyGraph.getViewport().setScrollable(true);
        mMoneyGraph.getViewport().setScalableY(true);
        mMoneyGraph.getViewport().setScrollableY(true);

        mMoneyGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        mMoneyGraph.setTitle("ClientBot money monitoring");
        mMoneyGraph.setTitleTextSize(50);

        mMoneyGraph.getLegendRenderer().setVisible(true);
        mMoneyGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mMoneyGraph.getLegendRenderer().setWidth(350);
        mMoneyGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

        mMoneySeriesMap = new HashMap<>();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_client_btn:
                ClientBotManager.getInstance().newClientBot(traderUpdateCallback);
                break;
        }
    }
}
