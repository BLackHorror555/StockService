package com.example.dmitron.stockservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ActivityUtils {

    public static SharedPreferences.Editor getSharedPrefEditor(Context context, String name){
        SharedPreferences sharedPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPref.edit();
        }
}
