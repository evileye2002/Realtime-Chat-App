package com.evileye2002.real_timechatapp.utilities;

import android.os.AsyncTask;

import com.evileye2002.real_timechatapp.listeners.TimestampListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Timestamp extends AsyncTask<Void, Void, Void> {
    String url = "https://time.is/vi/Hanoi";
    //String timestamp = "";
    Date timestamp;
    TimestampListener listener;
    //String patternRaw = "EEEE, dd MMMM, y";
    //String patternTarget = "EEE,dd,MMM,y";

    public Timestamp(TimestampListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        listener.onPost(timestamp);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String patternRaw = "HH:mm:ss, EEEE, dd MMMM, y";
        String patternTarget = "HH:mm:ss,EEE,dd,MMM,y";
        try {
            Document doc = Jsoup.connect(url).userAgent("Jsoup client").get();
            Elements timeNow = doc.select("time#clock");
            Elements dateNow = doc.select("div#dd");

            String[] pre = dateNow.text().toLowerCase().split(",");
            String mm = pre[1]
                    .replace(" một", " 1")
                    .replace(" hai", " 2")
                    .replace(" ba", " 3")
                    .replace(" tư", " 4")
                    .replace(" năm", " 5")
                    .replace(" sáu", " 6")
                    .replace(" bảy", " 7")
                    .replace(" tám", " 8")
                    .replace(" chín", " 9")
                    .replace(" mười", " 10")
                    .replace(" mười một", " 11")
                    .replace(" mười hai", " 12");
            String pre2 = timeNow.text() + ", " + pre[0] + "," + mm + "," + pre[2];
            timestamp = _funct.stringToDate(pre2, patternRaw);
            //String date = _funct.dateToString(dateRaw, patternTarget).toUpperCase();

            //timestamp = timeNow.text() + ";" + date;

        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
