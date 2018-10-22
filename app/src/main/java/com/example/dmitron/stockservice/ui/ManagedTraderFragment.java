package com.example.dmitron.stockservice.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.dmitron.stockservice.R;

public class ManagedTraderFragment extends Fragment implements View.OnClickListener {


    private Button createTraderButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_managed_trader, container, false);
        createTraderButton = v.findViewById(R.id.create_trader_button);


        createTraderButton.setOnClickListener(this);
        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.create_trader_button:
                Toast.makeText(getContext(), "Coming soon...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
