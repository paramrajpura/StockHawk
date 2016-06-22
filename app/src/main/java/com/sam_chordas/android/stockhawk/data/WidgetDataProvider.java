package com.sam_chordas.android.stockhawk.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.WidgetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 12-Jun-16.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Context mContext = null;
    private Cursor data = null;

    private static final String[] STOCK_COLUMNS = {
            QuoteDatabase.QUOTES + "." + QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_BIDPRICE = 2;
    static final int INDEX_STOCK_PERCENT_CHANGE = 3;
    static final int INDEX_STOCK_CHANGE = 4;
    static final int INDEX_STOCK_ISUP = 5;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position))
            return data.getLong(INDEX_STOCK_ID);
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        String stockSymbol = data.getString(INDEX_STOCK_SYMBOL);
        double bid = data.getDouble(INDEX_STOCK_BIDPRICE);
        double change = data.getDouble(INDEX_STOCK_CHANGE);

        remoteView.setTextViewText(R.id.change, String.valueOf(change));
        remoteView.setTextColor(R.id.stock_symbol,Color.BLACK);
        remoteView.setTextViewText(R.id.stock_symbol, stockSymbol);
        remoteView.setTextColor(R.id.bid_price,Color.BLACK);
        remoteView.setTextViewText(R.id.bid_price, String.valueOf(bid));

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Utils.QUOTE_SYMBOL,stockSymbol);
        remoteView.setOnClickFillInIntent(R.id.stock_symbol, fillInIntent);
        return remoteView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        data = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                STOCK_COLUMNS,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        Binder.restoreCallingIdentity(identityToken);
    }


    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }
}
