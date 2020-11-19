package edu.temple.androidcontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import edu.temple.androidcontentprovider.db.StockDBContract;
import edu.temple.androidcontentprovider.db.StockDBHelper;

public class StockDataProvider extends ContentProvider {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;
    Stock stock;

    boolean pauseFlag;

    public StockDataProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new StockDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Log.d("Selection", selection);
        Log.d("Arguments", selectionArgs[0]);

        Cursor c = getStockCursor(selection, selectionArgs);
        if (!(c.getCount() > 0)) {
            pauseFlag = true;
            downloadStockData(selectionArgs[0]);
            while (pauseFlag);
            saveData(stock.getSymbol(), stock.getName(), stock.getPrice());
            c = getStockCursor(selection, selectionArgs);
        }
        Log.d("Row count", String.valueOf(c.getCount()));
        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Search for stock data in database
    private Cursor getStockCursor(String selection, String[] selectionArgs){
        db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                StockDBContract.StockEntry.TABLE_NAME
                , new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_COMPANY}
                , selection
                , selectionArgs
                , null
                , null
                , null);
        c.moveToNext();

        return c;
    }

    // Download stock data
    public void downloadStockData(final String stockSymbol) {

        Thread t = new Thread() {

            public void run() {

                URL stockQuoteUrl;

                try

                {

                    stockQuoteUrl = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + stockSymbol);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    stockQuoteUrl.openStream()));

                    String response = "", tmpResponse;

                    tmpResponse = reader.readLine();
                    while (tmpResponse != null) {
                        response = response + tmpResponse;
                        tmpResponse = reader.readLine();
                    }

                    JSONObject stockObject = new JSONObject(response);
                    Message msg = Message.obtain();
                    msg.obj = stockObject;

                    Log.d("STOCK QUOTE OUTPUT", stockObject.toString());
                    stockHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    Handler stockHandler = new Handler (new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                JSONObject stockObject = (JSONObject) msg.obj;
                stock = new Stock(stockObject.getString("Name")
                        , stockObject.getString("Symbol")
                        ,stockObject.getDouble("LastPrice"));
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                pauseFlag = false;
            }
            return true;
        }
    });


    // Save stock data to database
    private void saveData(String symbol, String company, double price){

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, symbol);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_COMPANY, company);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_PRICE, price);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                StockDBContract.StockEntry.TABLE_NAME,
                null,
                values);

        if (newRowId > 0) {
            Log.d("Stock data saved ", newRowId + " - " + company);
        } else {
            Log.d("Stock data NOT saved ", newRowId + " - " + company);
        }

    }
}
