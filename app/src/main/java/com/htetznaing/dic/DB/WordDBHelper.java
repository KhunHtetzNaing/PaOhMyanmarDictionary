package com.htetznaing.dic.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.htetznaing.dic.Model.Model;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class WordDBHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "pao_mm_word.sqlite";
    private static final String MAIN_DB = "မာတိကာ";
    private static final int DATABASE_VERSION = 1;

    private static WordDBHelper dbHelper;
    private SQLiteDatabase db;

    private Context mContext;

    public WordDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static synchronized WordDBHelper getInstance(Context mContext) {
        if (dbHelper == null) {
            dbHelper = new WordDBHelper(mContext);
        }
        return dbHelper;
    }

    public void open() throws SQLException {
        this.db = getWritableDatabase();
    }

    public void close() {
        this.db.close();
    }

    public ArrayList<Model> getTableOfContents() throws SQLException {
        open();
        String query = "SELECT * FROM '"+MAIN_DB+"'";
        Cursor c = this.db.rawQuery(query, null);
        ArrayList<Model> data = new ArrayList<>();
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        Model model = new Model();
                        String mm = c.getString(0);
                        String paoh = c.getString(1);
                        String count = c.getString(2);
                        if (mm!=null && !mm.isEmpty() && paoh!=null && !paoh.isEmpty()) {
                            model.setMm(mm);
                            model.setPaoh(paoh);
                            model.setCount(count);
                            data.add(model);
                        }
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public ArrayList<Model> getByCategory(String category) throws SQLException {
        open();
        String query = "SELECT * FROM '"+category+"'";
        Cursor c = this.db.rawQuery(query, null);
        ArrayList<Model> data = new ArrayList<>();
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String mm = c.getString(0);
                        String paoh = c.getString(1);
                            Model model = new Model();
                            model.setMm(mm);
                            model.setPaoh(paoh);
                            data.add(model);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
