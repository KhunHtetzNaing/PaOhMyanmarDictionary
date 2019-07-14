package com.htetznaing.dic;

import com.htetznaing.dic.Utils.AIOmmTool;

public class Test {
    public static void main(String a[]){

        String userInput = "ကားမုဲင္ꩻ";

        if (userInput.contains("ꩻ")){
            userInput = userInput.replace("ꩻ","zMaiNgarz");
        }

        if (userInput.contains("ႏ")){
            userInput = userInput.replace("ႏ","zMaiPatNgarz");
        }

        String unicode = AIOmmTool.getUnicode(userInput);

        if (unicode.contains("zMaiNgarz")){
            unicode = unicode.replace("zMaiNgarz","ꩻ");
        }

        if (unicode.contains("zMaiPatNgarz")){
            unicode = unicode.replace("zMaiPatNgarz","ႏ");
        }

        System.out.println(unicode);
    }
}
