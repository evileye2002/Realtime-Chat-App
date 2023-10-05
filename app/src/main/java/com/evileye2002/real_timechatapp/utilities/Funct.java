package com.evileye2002.real_timechatapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.evileye2002.real_timechatapp.listeners.EditTextListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.type.DateTime;

import java.io.ByteArrayOutputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public class Funct {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static final String bitmapToString(Bitmap input) {
        Bitmap preview = Bitmap.createScaledBitmap(input, input.getWidth(), input.getHeight(), false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        preview.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static final Bitmap stringToBitmap(String input) {
        byte[] bytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static final String dateToString(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static final Date stringToDate(String sDate, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        return dateFormat.parse(sDate, pos);
    }

    public static final void onTextChange(EditText editText, TextInputLayout textInputLayout, final EditTextListener listener) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listener.onTextChange(s, textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
