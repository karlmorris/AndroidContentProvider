package edu.temple.androidcontentprovider.db;

import android.provider.BaseColumns;

public final class StockDBContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StockEntry.TABLE_NAME + " (" +
                    StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    StockEntry.COLUMN_NAME_SYMBOL + TEXT_TYPE + " collate nocase " + COMMA_SEP +
                    StockEntry.COLUMN_NAME_COMPANY + TEXT_TYPE + COMMA_SEP +
                    StockEntry.COLUMN_NAME_PRICE + " REAL" +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StockEntry.TABLE_NAME;

    public static abstract class StockEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_COMPANY = "company";
        public static final String COLUMN_NAME_PRICE = "price";
    }
}