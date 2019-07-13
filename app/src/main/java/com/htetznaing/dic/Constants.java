package com.htetznaing.dic;

import java.util.Random;

public class Constants {
    public static int getColor(){
        String a []= {"01bfa5","9C62ED","38d882","ffd900","19d3ea","FF6700","3DB2FF","fc00a1","8ed900"};
        String randomStr = a[new Random().nextInt(a.length)];
        int color = android.graphics.Color.parseColor("#"+randomStr);
        return color;
    }
}
