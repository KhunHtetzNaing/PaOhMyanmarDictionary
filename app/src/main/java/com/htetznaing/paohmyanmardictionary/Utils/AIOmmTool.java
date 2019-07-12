package com.htetznaing.paohmyanmardictionary.Utils;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.myanmartools.TransliterateU2Z;
import com.google.myanmartools.TransliterateZ2U;
import com.google.myanmartools.ZawgyiDetector;

public class AIOmmTool {
    private static final ZawgyiDetector detector = new ZawgyiDetector();
    private static final TransliterateZ2U zawgyi2Unicode = new TransliterateZ2U("Zawgyi to Unicode");
    private static final TransliterateU2Z unicode2Zawgyi = new TransliterateU2Z("Unicode to Zawgyi");

    public static String zawgyi2Unicode(String input){
        return zawgyi2Unicode.convert(input);
    }

    public static String unicode2Zawgyi(String input){
        return unicode2Zawgyi.convert(input);
    }

    public static String getUnicode(String input){
        double score = detector.getZawgyiProbability(input);
        if (score < 0.999){
            System.out.println("Unicode");
            return input;
        }else
            System.out.println("Zawgyi");
        return zawgyi2Unicode.convert(input);
    }

    public static String getZawgyi(String input){
        double score = detector.getZawgyiProbability(input);
        if (score < 0.999){
            System.out.println("Unicode");
            return unicode2Zawgyi.convert(input);
        }else
            System.out.println("Zawgyi");
        return input;
    }

    public static boolean isUnicode(Context context){
        TextView textView = new TextView(context, null);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        textView.setText("\u1000");
        textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int length1 = textView.getMeasuredWidth();

        textView.setText("\u1000\u1039\u1000");
        textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int length2 = textView.getMeasuredWidth();
        return length1 == length2;
    }
}
