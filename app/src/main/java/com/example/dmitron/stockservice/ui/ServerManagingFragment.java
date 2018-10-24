package com.example.dmitron.stockservice.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server.StockService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class ServerManagingFragment extends Fragment implements View.OnClickListener {

    //UI
    private TextView clientCountView;
    private Button startServiceButton, stopServiceButton;
    private GraphView graph;
    private HashMap<String, LineGraphSeries<DataPoint>> seriesMap;
    private Spinner spinner;
    private ListView productsListView;

    //utils
    private Random rnd;
    private ArrayAdapter<String> graphAdapter;
    private BroadcastReceiver clientCountReceiver, updateProductReceiver;
    private int lastX = 0;
    private ArrayAdapter<String> productsAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setClientCountReceiver(context);
        setProductUpdateReceiver(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_server_managing, container, false);

        clientCountView = v.findViewById(R.id.client_count_view);
        spinner = v.findViewById(R.id.spinner);
        startServiceButton = v.findViewById(R.id.start_service_btn);
        stopServiceButton = v.findViewById(R.id.stop_service_btn);
        graph = v.findViewById(R.id.graph);
        productsListView = v.findViewById(R.id.products_list_view);

        createGraph();
        initSpinner();
        initListView();

        startServiceButton.setOnClickListener(this);
        stopServiceButton.setOnClickListener(this);

        return v;
    }

    private void initListView() {
        productsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        productsListView.setAdapter(productsAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        rnd = new Random();
        super.onActivityCreated(savedInstanceState);
    }

    private void initSpinner() {
        graphAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        graphAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(graphAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String name = (String) parent.getItemAtPosition(position);
                graph.removeAllSeries();
                if (seriesMap.containsKey(name))
                    graph.addSeries(seriesMap.get(name));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void createGraph() {

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setMinY(0);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        graph.setTitle("Products monitoring");
        graph.setTitleTextSize(50);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setWidth(250);
        graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

        seriesMap = new HashMap<>();
    }


    /**
     * register receiver that get product updates from server
     */
    private void setProductUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.update_products_action));

        updateProductReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject products = new JSONObject(intent.getStringExtra("products"));
                    updateProductsInfo(products);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(updateProductReceiver, filter);
    }

    /**
     * update gtaph and product view with new data
     *
     * @param products new products
     * @throws JSONException error parsing json object
     */
    private void updateProductsInfo(JSONObject products) throws JSONException {
        Iterator<String> iterator = products.keys();
        productsAdapter.clear();
        while (iterator.hasNext()) {
            String product = iterator.next();

            if (!seriesMap.containsKey(product)) {
                //add new series
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                series.setTitle(product);
                seriesMap.put(product, series);

                //update graphAdapter
                graphAdapter.add(product);
            }

            //update series
            DataPoint newPoint = new DataPoint(lastX, products.getInt(product));
            seriesMap.get(product).appendData(newPoint, true, 20);
            lastX++;

            //update products list view
            productsAdapter.add(product + ", price - " + products.getInt(product));

            //productsView.append(product + " : " + products.get(product) + "\n");
        }
    }


    /**
     * Register receiver to receive intents with a new number of clients
     */
    private void setClientCountReceiver(Context context) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.client_count_action));

        clientCountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                clientCountView.setText(intent.getStringExtra("client_count"));
            }

        };
        LocalBroadcastManager.getInstance(context).registerReceiver(clientCountReceiver, filter);
    }

    public void onStartService() {
        Intent intent = new Intent(getActivity(), StockService.class);
        getActivity().startService(intent);
        startServiceButton.setEnabled(false);
        stopServiceButton.setEnabled(true);
        spinner.setVisibility(View.VISIBLE);

    }

    public void onStopService() {
        getActivity().stopService(new Intent(getActivity(), StockService.class));

        startServiceButton.setEnabled(true);
        stopServiceButton.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(clientCountReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateProductReceiver);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.start_service_btn:
                onStartService();
                break;
            case R.id.stop_service_btn:
                onStopService();
                break;
        }
    }
}
