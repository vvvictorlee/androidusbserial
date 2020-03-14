package com.example.jack8.floatwindow;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.util.Pair;

import com.jack8.floatwindow.Window.WindowConfig;

public class TemperatureParameters {
    private static final String MIN_LIMIT = "min";
    private static final String MAX_LIMIT = "max";
    private static final String TEMPERATURE_PARA = "temperatureparameter";

    private static float MIN_LIMIT_TEMP = .0f;
    private static float MAX_LIMIT_TEMP = .0f;
    private static final float THRESHOLD = .01f;

    public static float getMaxLimit(Context context){
        if (Math.abs(MAX_LIMIT_TEMP - .0f) < THRESHOLD)
            MAX_LIMIT_TEMP = context.getSharedPreferences(TEMPERATURE_PARA,0).getFloat(MAX_LIMIT,40);
        return MAX_LIMIT_TEMP;
    }

    public static void setMaxLimit(Context context,@FloatRange(from = 30,to=40) float maxLimit){
        MAX_LIMIT_TEMP = maxLimit;
        context.getSharedPreferences(TEMPERATURE_PARA,0).edit().putFloat(MAX_LIMIT,maxLimit).apply();
    }

    public static float getMinLimit(Context context){
        if (Math.abs(MIN_LIMIT_TEMP - .0f) < THRESHOLD)
            MIN_LIMIT_TEMP = context.getSharedPreferences(TEMPERATURE_PARA,0).getFloat(MIN_LIMIT,30);
        return MIN_LIMIT_TEMP;
    }

    public static void setMinLimit(Context context,@FloatRange(from = 30,to=40) float minLimit){
        MIN_LIMIT_TEMP = minLimit;
        context.getSharedPreferences(TEMPERATURE_PARA,0).edit().putFloat(MIN_LIMIT,minLimit).apply();
    }



}
