package com.example.dmitron.stockservice.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.dmitron.stockservice.R;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ServerManagingFragment serverManagingFragment;
    private ClientMonitoringFragment clientMonitoringFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initNavDrawer();

        //setting up fragment
        if (findViewById(R.id.fragment_container) != null){
            if (savedInstanceState != null)
                return;

            serverManagingFragment = new ServerManagingFragment();
            clientMonitoringFragment = new ClientMonitoringFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, serverManagingFragment)
                    .add(R.id.fragment_container, clientMonitoringFragment)
                    .hide(clientMonitoringFragment)
                    .commit();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    private void initNavDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()){
                    case R.id.server_managing:
                        showFragment(serverManagingFragment);
                        break;
                    case R.id.clients_monitoring:
                        showFragment(clientMonitoringFragment);

                        break;
                }

                return true;
            }
        });
    }

    private void showFragment(Fragment showFragment){
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment fragment : fm.getFragments()) {
            if (fragment == showFragment)
                fm.beginTransaction()
                .show(fragment)
                .commit();
            else
                fm.beginTransaction()
                        .hide(fragment)
                        .commit();

        }
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}