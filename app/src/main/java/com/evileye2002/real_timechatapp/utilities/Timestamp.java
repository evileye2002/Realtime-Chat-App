package com.evileye2002.real_timechatapp.utilities;

import android.os.AsyncTask;

import com.evileye2002.real_timechatapp.listeners.TimestampListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Timestamp extends AsyncTask<Void, Void, Void> {
    String url = "https://time.is/vi/Hanoi";
    String timestamp = "";
    TimestampListener listener;

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
        try {
            Document doc = Jsoup.connect(url).userAgent("Jsoup client").get();
            Elements timeNow = doc.select("time#clock");
            Elements dateNow = doc.select("div#dd");

            String date = dateNow.text().replace("Tháng ","Thg.").replace("tuần ","T.");
            timestamp = timeNow.text() + ";" + date;

        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
