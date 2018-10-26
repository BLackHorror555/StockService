package com.example.dmitron.stockservice.client.client_bot_monitoring;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TradersListAdapter extends BaseAdapter {

    SparseArray<String> mTraders;
    private Context mContext;

    TradersListAdapter(Context context){
        mTraders = new SparseArray<>();
        mContext = context;
    }

    @Override
    public int getCount() {
        return mTraders.size();
    }

    @Override
    public Object getItem(int position) {
        return mTraders.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return mTraders.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView traderNameView = new TextView(mContext);
        traderNameView.setText("Trader + " + getItemId(position));
        return traderNameView;
    }

    public void remove(){

    }
}
