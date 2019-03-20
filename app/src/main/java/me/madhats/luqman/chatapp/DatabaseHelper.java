package me.madhats.luqman.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SecureChat";
    public  static final String TABLE_NAME = "UserKeys";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, EMAIL TEXT, KEY TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData (String email, String key){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT KEY from "+TABLE_NAME+" where EMAIL = ?", new String[] {email});
        System.out.println("ENTRY...........!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            getWritableDatabase().delete(TABLE_NAME, "EMAIL = ?", new String[] {email});
            System.out.println("DELETED ONE ENTRY...........!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        ContentValues content_values = new ContentValues();
        content_values.put("EMAIL",email);
        content_values.put("KEY",key);
        if(db.insert(TABLE_NAME, null, content_values) != -1)
            return false;
        else
            return true;

    }

    public Cursor getData (String email){
        System.out.println("INSIDE GETDATA DATABASE HELPER......................................................."+email);
        Cursor cursor = getReadableDatabase().rawQuery("SELECT KEY from "+TABLE_NAME+" where EMAIL = ?", new String[] {email});
        System.out.println("INSIDE GETDATA DATABASE HELPER.......................................................ENDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        return cursor;
    }
}