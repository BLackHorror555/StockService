package com.example.dmitron.stockservice.utils;

import android.graphics.Color;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class GraphHelper {

    static Random rnd = new Random();

    /**
     * creates new line graph series
     * @param title name of created series
     * @return series
     */
    public static LineGraphSeries<DataPoint> newLineGraphSeries(String title){
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        series.setColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        series.setTitle(title);
        return series;
    }
}
