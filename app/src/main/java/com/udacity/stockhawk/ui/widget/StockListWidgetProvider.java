package com.udacity.stockhawk.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

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

public class StockListWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock_list);

            PendingIntent mainIntent = makeMainIntent(context, appWidgetId);
            remoteViews.setOnClickPendingIntent(R.id.tv_stock_header, mainIntent);

            PendingIntent listItemIntentTemplate = makeListItemIntentTemplate(context, appWidgetId);
            remoteViews.setPendingIntentTemplate(R.id.lv_stock_list, listItemIntentTemplate);

            setRemoteAdapter(remoteViews, context);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {
            case QuoteSyncJob.ACTION_DATA_UPDATED:
                notifyWidgetDataChanged(context);
                break;
        }
    }

    private PendingIntent makeMainIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        // When intents are compared, the extras are ignored, so we need to embed the extras
        // into the data so that the extras will not be ignored.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private PendingIntent makeListItemIntentTemplate(Context context, int appWidgetId) {
        Intent intent = new Intent(context, DetailActivity.class);
        // When intents are compared, the extras are ignored, so we need to embed the extras
        // into the data so that the extras will not be ignored.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setRemoteAdapter(RemoteViews remoteViews, Context context) {
        Intent remoteViewsServiceIntent = new Intent(context, StockListWidgetRemoteViewsService.class);
        remoteViews.setRemoteAdapter(R.id.lv_stock_list, remoteViewsServiceIntent);
    }

    private void notifyWidgetDataChanged(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_stock_list);
    }
}
