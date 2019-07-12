package com.htetznaing.paohmyanmardictionary.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.htetznaing.paohmyanmardictionary.Model.Model;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class DictionaryDBHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "pao_mm_dictionary.sqlite";
    private static final String DB = "pao_mm_dictionary";
    private static final int DATABASE_VERSION = 1;

    private static DictionaryDBHelper dbHelper;
    private SQLiteDatabase db;

    private Context mContext;

    public DictionaryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static synchronized DictionaryDBHelper getInstance(Context mContext) {
        if (dbHelper == null) {
            dbHelper = new DictionaryDBHelper(mContext);
        }
        return dbHelper;
    }

    public void open() throws SQLException {
        this.db = getWritableDatabase();
    }

    public void close() {
        this.db.close();
    }

    public ArrayList<Model> searchWord(String word) throws SQLException {
        open();
        String query = "SELECT * FROM "+DB+" WHERE pao LIKE '%"+word+"%' OR mm LIKE '%"+word+"%'";
        Cursor c = this.db.rawQuery(query, null);
        ArrayList<Model> data = new ArrayList<>();
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        Model model = new Model();
                        String paoh = c.getString(1);
                        String mm = c.getString(2);
                        model.setPaoh(paoh);
                        model.setMm(mm);
                        data.add(model);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public ArrayList<Model> getAll() throws SQLException {
        open();
        String query = "SELECT * FROM "+DB;
        Cursor c = this.db.rawQuery(query, null);
        ArrayList<Model> data = new ArrayList<>();
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        Model model = new Model();
                        String paoh = c.getString(1);
                        String mm = c.getString(2);
                        model.setPaoh(paoh);
                        model.setMm(mm);
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
