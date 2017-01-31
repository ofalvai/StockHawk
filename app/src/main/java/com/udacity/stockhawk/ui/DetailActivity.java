package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_QUOTE_SYMBOL = "quote_symbol";

    private static final int LOADER_ID_STOCKS = 100;

    private String mSymbol;

    /**
     * List of history entries. Each entry is a pair of a Long timestamp and a Float price
     */
    private List<Pair<Long, Float>> mHistory;

    private DateFormat mDateFormat;

    @BindView(R.id.chart_quote_history)
    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mDateFormat = android.text.format.DateFormat.getDateFormat(this);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_QUOTE_SYMBOL)) {
                mSymbol = getIntent().getExtras().getString(EXTRA_QUOTE_SYMBOL);
                getSupportActionBar().setTitle(mSymbol);
                getSupportLoaderManager().initLoader(LOADER_ID_STOCKS, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = Contract.Quote.COLUMN_SYMBOL + " = ?";
        return new CursorLoader(
                this,
                Contract.Quote.URI,
                new String[] { Contract.Quote.COLUMN_HISTORY },
                selection,
                new String[] { mSymbol },
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;
        if (data.moveToFirst()) {
            String rawHistory = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            mHistory = QuoteSyncJob.parseQuoteHistory(rawHistory);
            displaChartData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void displaChartData() {
        List<Entry> entries = new ArrayList<>();

        for (Pair<Long, Float> pair : mHistory) {
            // Timestamp goes to the X axis, price to Y axis
            entries.add(new Entry(pair.first / 1000, pair.second));
        }

        // Entries need to be ascending, but the history data returned is descending
        Collections.reverse(entries);

        LineDataSet dataSet = new LineDataSet(entries, mSymbol);

        LineData data = new LineData(dataSet);
        mChart.setData(data);

        // Chart colors
        @ColorRes int textColor = getResources().getColor(R.color.chart_text_color);
        mChart.getXAxis().setTextColor(textColor);
        mChart.getAxisRight().setTextColor(textColor);
        mChart.getLegend().setTextColor(textColor);

        // Axis formatters
        mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date((long) value);
                return mDateFormat.format(date);
            }
        });

        mChart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "$" + value;
            }
        });

        // Removing default description text
        Description description = new Description();
        description.setText("");
        mChart.setDescription(description);

        // Removing left axis
        mChart.getAxisLeft().setEnabled(false);

        // Draw
        mChart.invalidate();
    }
}
