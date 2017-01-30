package com.udacity.stockhawk.ui.widget;
/*
 * Copyright 2017. 01. 30. Olivér Falvai
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockListWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;

    private Cursor mCursor = null;

    private DecimalFormat mDollarFormat;

    public StockListWidgetRemoteViewsFactory(Context context) {
        mContext = context;
        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();

        Uri stockListUri = Contract.Quote.URI;
        mCursor = mContext.getContentResolver().query(
                stockListUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}), // Projection
                null, // Selection
                null, // Selection args
                Contract.Quote.COLUMN_SYMBOL // Sort
        );

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || mCursor == null) {
            return null;
        }

        if (!mCursor.moveToPosition(position)) {
            return null;
        }

        String symbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
        float price = mCursor.getFloat(Contract.Quote.POSITION_PRICE);
        String formattedPrice = mDollarFormat.format(price);

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget_stock);

        remoteViews.setTextViewText(R.id.tv_list_item_widget_stock_symbol, symbol);
        remoteViews.setTextViewText(R.id.tv_list_item_widget_stock_price, formattedPrice);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget_stock);
        views.setTextViewText(R.id.tv_list_item_widget_stock_symbol, mContext.getString(R.string.widget_list_item_loading));
        return views;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
