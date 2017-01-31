package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_QUOTE_SYMBOL = "quote_symbol";

    private static final int LOADER_ID_STOCKS = 100;

    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_QUOTE_SYMBOL)) {
                mSymbol = getIntent().getExtras().getString(EXTRA_QUOTE_SYMBOL);
                getSupportLoaderManager().initLoader(LOADER_ID_STOCKS, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = Contract.Quote.COLUMN_SYMBOL + " = ?";
        Timber.d("Selection: " + selection);
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
            String historyRaw = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
