package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class DisplayGraphActivity extends AppCompatActivity {

    private final String LOG_TAG = "DisplayGraphActivity";
    private LineChart chart;
    private ArrayList<Entry> stockPriceList = new ArrayList<>();
    private ArrayList<String> dateList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String quoteSymbol = intent.getStringExtra(Utils.QUOTE_SYMBOL);
        setTitle(quoteSymbol);
        FetchHistoricData fetchTask = new FetchHistoricData();
        fetchTask.execute(quoteSymbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_line_graph);
        super.onCreate(savedInstanceState);
    }

    public class FetchHistoricData extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            chart = (LineChart) findViewById(R.id.DailyChart);
            LineDataSet priceData = new LineDataSet(stockPriceList,
                    getString(R.string.desc_graph));
            priceData.setDrawCircles(true);
            priceData.setDrawValues(false);
            LineData graphData = new LineData(dateList,priceData);
            if (chart != null) {
                chart.getAxisRight().setEnabled(false);
                chart.setData(graphData);
                chart.setDescription("");
                Legend l = chart.getLegend();
                l.setTextColor(Color.WHITE);

                YAxis y = chart.getAxisLeft();
                y.setTextColor(Color.WHITE);
                y.setDrawLimitLinesBehindData(false);
                y.setDrawGridLines(false);

                XAxis x = chart.getXAxis();
                x.setTextColor(Color.WHITE);
                x.setPosition(XAxis.XAxisPosition.BOTTOM);

                chart.invalidate();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... params) {
            Uri.Builder buildURL = new Uri.Builder();
            buildURL.scheme("https").authority("query.yahooapis.com").appendPath("v1").
                    appendPath("public").appendPath("yql");
            String CALLBACK_VALUE = "";
            String FORMAT_KEY = "format";
            String FORMAT_VALUE = "json";
            String DIAG_VALUE = "true";
            String CALLBACK_KEY = "callback";
            String ENV_VALUE = "store://datatables.org/alltableswithkeys";
            String QUERY_KEY = "q";
            String DIAG_KEY = "diagnostics";
            String ENV_KEY = "env";

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            Date endDate = new Date();
            String fEndDate = format.format(endDate);

            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.add(Calendar.MONTH, -1);
            Date startDate = c.getTime();
            String fStartDate = format.format(startDate);
            buildURL.appendQueryParameter(QUERY_KEY,
                    "select * from yahoo.finance.historicaldata where symbol='" +
                            params[0] + "' and startDate = '" +fStartDate +
                            "' and endDate = '"+fEndDate+"'")
                    .appendQueryParameter(FORMAT_KEY, FORMAT_VALUE)
                    .appendQueryParameter(DIAG_KEY, DIAG_VALUE)
                    .appendQueryParameter(ENV_KEY, ENV_VALUE)
                    .appendQueryParameter(CALLBACK_KEY, CALLBACK_VALUE).build();
            String quoteHistoricData = urlConnectFetchData(buildURL.build().toString());

            try {
                JSONObject jsonObj1 = new JSONObject(quoteHistoricData);
                JSONObject jsonObj2 = jsonObj1.getJSONObject("query").getJSONObject("results");
                JSONArray jsonArray = jsonObj2.getJSONArray("quote");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject dailyData = jsonArray.getJSONObject(i);
                    String date = dailyData.getString("Date");
                    dateList.add(date);
                    stockPriceList.add(new Entry(Float.parseFloat(
                            dailyData.getString("Adj_Close")),jsonArray.length()-i-1));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Collections.reverse(dateList);
            return null;
        }

        String urlConnectFetchData(String builtUrl){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(builtUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}
