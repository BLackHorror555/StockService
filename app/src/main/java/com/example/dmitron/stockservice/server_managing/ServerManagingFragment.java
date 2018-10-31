package com.example.dmitron.stockservice.server_managing;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.utils.GraphHelper;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class ServerManagingFragment extends Fragment implements View.OnClickListener, ServerManagingContract.View {

    //UI
    private TextView mClientCountView;
    private Button mStartServiceButton, mStopServiceButton;
    private GraphView mGraph;
    private HashMap<String, LineGraphSeries<DataPoint>> mSeriesMap;
    private Spinner mSpinner;
    private ListView mProductsListView;

    private ArrayAdapter<String> mSpinnerAdapter;
    private ArrayAdapter<String> mProductsListAdapter;
    private int mLastX = 0;


    private ServerManagingContract.Presenter mPresenter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        new ServerManagingPresenter(this, context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_server_managing, container, false);

        mClientCountView = root.findViewById(R.id.client_count_view);
        mSpinner = root.findViewById(R.id.spinner);
        mStartServiceButton = root.findViewById(R.id.start_service_btn);
        mStopServiceButton = root.findViewById(R.id.stop_service_btn);
        mGraph = root.findViewById(R.id.graph);
        mProductsListView = root.findViewById(R.id.products_list_view);

        createGraph();
        initSpinner();
        initListView();

        mStartServiceButton.setOnClickListener(this);
        mStopServiceButton.setOnClickListener(this);

        mPresenter.viewCreated();
        return root;
    }

    /**
     * initialise product list view with adapter
     */
    private void initListView() {
        mProductsListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mProductsListView.setAdapter(mProductsListAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * initialise mSpinner with adapter and item selected listener
     */
    private void initSpinner() {
        mSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ProductType name = ProductType.valueOf((String) parent.getItemAtPosition(position));
                mPresenter.productToMonitoringSelected(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * setup mGraph view
     */
    private void createGraph() {

        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(40);
        mGraph.getViewport().setMinY(0);

        mGraph.getViewport().setScalable(true);
        mGraph.getViewport().setScrollable(true);
        mGraph.getViewport().setScalableY(true);
        mGraph.getViewport().setScrollableY(true);

        mGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        mGraph.setTitle("Products monitoring");
        mGraph.setTitleTextSize(50);

        mGraph.getLegendRenderer().setVisible(true);
        mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraph.getLegendRenderer().setWidth(250);
        mGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

        mSeriesMap = new HashMap<>();
    }

    /**
     * start background service
     */
    public void onStartService() {
        mPresenter.startService();
    }


    /**
     * stop service
     */
    public void onStopService() {
        mPresenter.stopService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPresenter.detached();
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

    @Override
    public void setPresenter(ServerManagingContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showConnectedClientsCount(int clientCount) {
        mClientCountView.setText(String.format(Locale.getDefault(), "%d", clientCount));
    }

    @Override
    public void updateProductsInfo(JSONObject products) throws JSONException {
        mProductsListAdapter.clear();
        for (Iterator<String> it = products.keys(); it.hasNext(); ) {

            String product = it.next();
            if (!mSeriesMap.containsKey(product)) {

                mSeriesMap.put(product, GraphHelper.newLineGraphSeries(product));
                mSpinnerAdapter.add(product);
            }

            //update series
            DataPoint newPoint = new DataPoint(mLastX, products.getInt(product));
            mSeriesMap.get(product).appendData(newPoint, true, 20);
            mLastX++;

            //update products list view
            mProductsListAdapter.add(product + ", price - " + products.get(product));

        }
    }

    @Override
    public void setStartButtonEnabling(boolean isEnabled) {
        mStartServiceButton.setEnabled(isEnabled);
    }

    @Override
    public void setStopButtonEnabling(boolean isEnabled) {
        mStopServiceButton.setEnabled(isEnabled);
    }

    @Override
    public void showProductAtGraph(ProductType productType) {
        mGraph.removeAllSeries();
        if (mSeriesMap.containsKey(productType.name()))
            mGraph.addSeries(mSeriesMap.get(productType.name()));
    }

    @Override
    public void showToastMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
