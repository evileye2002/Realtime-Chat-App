package com.evileye2002.real_timechatapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Funct {
    public static void showToast(Context context,String message ){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static final String bitmapToString(Bitmap input){
        String output = "";

        return output;
    }

    public static final Bitmap stringToBitmap(String input){
        byte[] bytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static final String dateToString(Date date, String format){
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static final Date stringToDate(String sDate, String format){
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        return dateFormat.parse(sDate,pos);
    }
}
