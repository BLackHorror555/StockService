package com.example.dmitron.stockservice.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dmitron.stockservice.client.Client;
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

public class MainActivity extends AppCompatActivity {

    private boolean isBound = false;
    private TextView clientCountView;
    private TextView productsView;
    private int clients = 0;
    StockService stockService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            stockService = ((StockService.StockBinder) service).getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stockService = null;
            isBound = false;
        }
    };
    private BroadcastReceiver clientCountReceiver, updateProductReceiver;



    private GraphView graph;
    //private ArrayList<LineGraphSeries<DataPoint>> seriesArray;
    //map with fields product name : graph series
    private HashMap<String, LineGraphSeries<DataPoint>> seriesMap;
    private int lastX = 0;

    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    //utils
    private Random rnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setClientCountReceiver();
        setProductUpdateReceiver();

        //UI
        clientCountView = findViewById(R.id.client_count_view);
        productsView = findViewById(R.id.products_view);
        spinner = findViewById(R.id.spinner);

        rnd = new Random();

        createGraph();
        initSpinner();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initSpinner() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //adapter.add(getString(R.string.all_products));

        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                graph.removeAllSeries();
                /*if (name.equals(getString(R.string.all_products))){
                    for (LineGraphSeries<DataPoint> series : seriesMap.values()) {
                        graph.addSeries(series);
                    }
                }
                else {
                    graph.addSeries(seriesMap.get(name));
                }*/
                if (seriesMap.containsKey(name))
                    graph.addSeries(seriesMap.get(name));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void createGraph() {
        graph = findViewById(R.id.graph);

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


    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * register receiver that get product updates from server
     */
    private void setProductUpdateReceiver() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.update_products_receiver));

        updateProductReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (getString(R.string.update_products_receiver).equals(intent.getAction())) {
                    productsView.setText("");

                    try {
                        JSONObject products = new JSONObject(intent.getStringExtra("products"));

                        Iterator<String> iterator = products.keys();
                        while (iterator.hasNext()) {
                            String product = iterator.next();

                            if (!seriesMap.containsKey(product)){
                                //add new series
                                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                                series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                                series.setTitle(product);
                                seriesMap.put(product, series);

                                //update adapter
                                adapter.add(product);
                            }

                            //update series
                            DataPoint newPoint = new DataPoint(lastX, products.getInt(product));
                            seriesMap.get(product).appendData(newPoint, true, 20);
                            lastX++;

                            //update products view
                            productsView.append(product + " : " + products.get(product) + "\n");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        };
        registerReceiver(updateProductReceiver, filter);
    }

    /*private void updateSeries(JSONObject products) throws JSONException {
        //append new data to all series's
        Iterator<String> iterator = products.keys();

        for (Series series : graph.getSeries()) {
            if (iterator.hasNext()) {
                String product = iterator.next();
                DataPoint newPoint = new DataPoint(lastX, products.getInt(product));
                ((LineGraphSeries<DataPoint>) series).appendData(newPoint, true, 20);
                ((LineGraphSeries<DataPoint>) series).setTitle(product);
                lastX++;
            }

        }
    }*/

    public void onNewClient(View view) {
        new Thread(new Client()).start();
    }


    /**
     * Register receiver to receive intents with a new number of clients
     */
    private void setClientCountReceiver() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(getString(R.string.client_count_action));

        clientCountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (getString(R.string.client_count_action).equals(intent.getAction()))
                    clientCountView.setText(intent.getStringExtra("client_count"));
            }
        };
        registerReceiver(clientCountReceiver, filter);
    }

    public void onStartService(View view) {
        Intent intent = new Intent(this, StockService.class);
        startService(intent);
        findViewById(R.id.start_service_btn).setEnabled(false);
        findViewById(R.id.stop_service_btn).setEnabled(true);
        findViewById(R.id.new_client_btn).setEnabled(true);
        spinner.setVisibility(View.VISIBLE);

    }

    public void onStopService(View view) {
        stopService(new Intent(this, StockService.class));

        findViewById(R.id.start_service_btn).setEnabled(true);
        findViewById(R.id.stop_service_btn).setEnabled(false);
        findViewById(R.id.new_client_btn).setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(clientCountReceiver);
        unregisterReceiver(updateProductReceiver);
    }
}