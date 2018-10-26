package com.example.dmitron.stockservice.client.client_bot_monitoring;

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
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dmitron.stockservice.R;
import com.example.dmitron.stockservice.client.Trader;
import com.example.dmitron.stockservice.server_managing.data.stock.ProductType;
import com.example.dmitron.stockservice.utils.GraphHelper;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;

public class ClientBotManagingFragment extends Fragment implements View.OnClickListener, ClientBotMonitoringContract.View {

    private GraphView mMoneyGraph;
    private HashMap<String, LineGraphSeries<DataPoint>> mMoneySeriesMap;
    private int mLastX = 0;

    private ClientBotMonitoringContract.Presenter mPresenter;

    //UI
    private Button mNewClientButton;
    private Spinner mSpinner;
    private ArrayAdapter<String> mSelectTraderAdapter;
    private TextView mProductsView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        new ClientBotMonitoringPresenter(this, context);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_client_bot_monitoring, container, false);

        mNewClientButton = v.findViewById(R.id.new_client_btn);
        mMoneyGraph = v.findViewById(R.id.client_money_graph);
        mSpinner = v.findViewById(R.id.client_spinner);
        mProductsView = v.findViewById(R.id.products_view);

        mNewClientButton.setOnClickListener(this);

        createGraphs();
        initSpinner();
        return v;
    }

    /**
     * initialise mSpinner with mSelectTraderAdapter and item selected list
     */
    private void initSpinner() {
        mSelectTraderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        mSelectTraderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSelectTraderAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String traderId = ((String) parent.getItemAtPosition(position)).split(" ")[1];
                mPresenter.clientSelected(traderId);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /**
     * put new data points with money into graph
     *
     * @param id    trader id
     * @param money trader money
     */
    private void updateMoneyGraph(String id, int money) {

        //add client if he is not
        if (!mMoneySeriesMap.containsKey(id)) {

            mMoneySeriesMap.put(id, GraphHelper.newLineGraphSeries("Trader" + id));
            mSelectTraderAdapter.add("Trader " + id);
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
                mPresenter.newBotClient();
                break;
        }
    }

    @Override
    public void setPresenter(ClientBotMonitoringContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showTraderInfo(Trader trader) {
        updateMoneyGraph(Integer.toString(trader.getId()), trader.getMoney());
        Map<ProductType, Integer> products = trader.getProducts();
        mProductsView.setText("");
        for (ProductType productType : products.keySet()) {
            mProductsView.append(productType.name() + " - " + products.get(productType) + "\n");
        }
    }

    @Override
    public void showTraderDisconnected(int traderId) {
        mSelectTraderAdapter.remove("Trader " + traderId);
        mSelectTraderAdapter.insert("Trader " + traderId + " - disconnected", mSelectTraderAdapter.getCount());
    }

    @Override
    public void showTraderOnGraph(String traderId) {
        mMoneyGraph.removeAllSeries();
        if (mMoneySeriesMap.containsKey(traderId))
            mMoneyGraph.addSeries(mMoneySeriesMap.get(traderId));
    }
}
