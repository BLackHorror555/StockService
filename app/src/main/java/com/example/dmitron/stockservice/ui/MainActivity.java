package com.example.dmitron.stockservice.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dmitron.stockservice.Client.Client;
import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server.StockService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.json.JSONException;
import org.json.JSONObject;

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


    private Handler handler = new Handler();
    private GraphView graph;
    //private ArrayList<LineGraphSeries<DataPoint>> seriesArray;
    private int lastX = 0;

    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setClientCountReceiver();
        setProductUpdateReceiver();

        //UI
        clientCountView = findViewById(R.id.client_count_view);
        productsView = findViewById(R.id.products_view);


        createGraph();
    }

    private void createGraph() {
        graph = findViewById(R.id.graph);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setMinY(0);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        graph.setTitle("Products monitoring");
        graph.setTitleTextSize(50);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setWidth(250);
        graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
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
                        while (iterator.hasNext()){
                            String product = iterator.next();
                            productsView.append(product + " : " + products.get(product) + "\n");
                        }

                        //add new series
                        Random rnd = new Random();
                        while (graph.getSeries().size() < products.length()){
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                            series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                            graph.addSeries(series);
                        }

                        //append new data to all series's
                        iterator = products.keys();

                        for (Series series : graph.getSeries()){
                            if (iterator.hasNext()) {
                                String product = iterator.next();
                                DataPoint newPoint = new DataPoint(lastX, products.getInt(product));
                                ((LineGraphSeries<DataPoint>) series).appendData(newPoint, true, 20);
                                ((LineGraphSeries<DataPoint>) series).setTitle(product);
                                lastX++;
                            }

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        };
        registerReceiver(updateProductReceiver, filter);
    }

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
        //bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        findViewById(R.id.start_service_btn).setEnabled(false);
        findViewById(R.id.stop_service_btn).setEnabled(true);
    }

    public void onStopService(View view) {
        stopService(new Intent(this, StockService.class));

        findViewById(R.id.start_service_btn).setEnabled(true);
        findViewById(R.id.stop_service_btn).setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(clientCountReceiver);
        unregisterReceiver(updateProductReceiver);
    }
}