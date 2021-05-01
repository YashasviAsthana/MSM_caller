package com.projects.mara.msm_caller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yashasvi on 02-06-2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_name = "CallLog.db";
    public static final String Table_name = "Log_Table";
    public static final String COL_1  = "SNo";
    //public static final String DB_table = "log";
    public static final String COL_2 = "Name";
    public static final String COL_3 = "Time";
    public static final String COL_4 = "Date";
    public static final String COL_5 = "Bal";
    public static final String COL_6 = "Phone";


    public DatabaseHelper(Context context){
        super(context, DB_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table "+Table_name+" (SNo INTEGER PRIMARY KEY AUTOINCREMENT, Name text, Time text, Date text, Bal text, Phone text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS "+Table_name);
        onCreate(db);
    }

    public boolean insertData(logdetails logdetails){
        SQLiteDatabase insdb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, logdetails.getName());
        contentValues.put(COL_3, logdetails.getCallTime());
        contentValues.put(COL_4, logdetails.getDate());
        contentValues.put(COL_5, logdetails.getBalance());
        contentValues.put(COL_6, logdetails.getPhone_no());
        //String quer = "INSERT INTO Log_Table(Name,Time,Date) VALUES('"+logdetails.getName()+"','"+logdetails.getCallTime()+"','"+logdetails.getDate()+"')";
        //insdb.execSQL(quer);
        long result = insdb.insert(Table_name,null,contentValues);
        insdb.close();
        return result != -1;
    }
    public List<logdetails> getRecords(){
         SQLiteDatabase db = this.getReadableDatabase();
         List<logdetails> mylist = new ArrayList<logdetails>();
         String query = "SELECT * FROM "+Table_name;
         Cursor cursor = db.rawQuery(query,null);
         if (cursor.moveToFirst()){
             do {
                 logdetails ld = new logdetails();
                 ld.setName(cursor.getString(1));
                 ld.setCallTime(cursor.getString(2));
                 ld.setDate(cursor.getString(3));
                 ld.setBalance(cursor.getString(4));
                 ld.setPhone(cursor.getString(5));
                 mylist.add(ld);
             } while (cursor.moveToNext());
         }
         close();

         return mylist;
     }

    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ Table_name);
        //db.execSQL("TRUNCATE table " + Table_name);
        db.close();
    }

    public void delete(String name,String date,String time){
        SQLiteDatabase db = this.getWritableDatabase();
        //int test = db.delete(Table_name,"Name=? , Date=? and Time=?",new String[]{name,date,time});

        db.execSQL("delete from "+ Table_name + " where Name='"+name+"' and Date='"+date+"' and Time='"+time+"'");
        //Log.e("Inside del",String.valueOf(test));
        db.close();
    }
}
