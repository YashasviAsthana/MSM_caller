package com.projects.mara.msm_caller;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallLog extends AppCompatActivity {

    ListView listView;
    LogAdapter logAdapter;
    List<logdetails> list,listtoshow;
    DatabaseHelper db;
    int position;
    String name,date,time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);
        getSupportActionBar().setTitle("Call Log");
        list = new ArrayList<logdetails>();
        listView = (ListView)findViewById(R.id.log_list);

        db = new DatabaseHelper(this);
        //retrieve database
        try {
            list = db.getRecords();
        }catch (NullPointerException e){
            Log.i("Exception","Null Pointer Ex");
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String number = listtoshow.get(i).getPhone_no();
                Intent intent = new Intent(CallLog.this,MainActivity.class);
                intent.putExtra("call","1");
                intent.putExtra("phone",number);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                position = i;
                name = list.get(i).getName();
                date = list.get(i).getDate();
                time = list.get(i).getCallTime();
                //Toast.makeText(CallLog.this,String.valueOf(i),Toast.LENGTH_SHORT).show();
                FragmentManager deletemanager = getFragmentManager();
                DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
                deleteDialogFragment.show(deletemanager, "theDialog");


                return true;
            }
        });
        listtoshow = list;
        Collections.reverse(listtoshow);
        //listtoshow is reverse of list now
        logAdapter = new LogAdapter(listtoshow,this);
        logAdapter.notifyDataSetChanged();
        listView.setAdapter(logAdapter);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logmenu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.clear:
                FragmentManager clearmanager = getFragmentManager();
                ClearDialogFragment clearDialogFragment = new ClearDialogFragment();
                clearDialogFragment.show(clearmanager, "theDialog");
                return true;
            case android.R.id.home:
                //overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.logout:
                FragmentManager logoutmanager = getFragmentManager();
                LogoutDialogFragment logoutDialogFragment = new LogoutDialogFragment();
                logoutDialogFragment.show(logoutmanager, "theDialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void clear(){
        db.deleteAll();
        listtoshow.clear();
        logAdapter.notifyDataSetChanged();
    }
    public void logout(){
        //logout-------------------------------
        SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //Getting editor
        SharedPreferences.Editor editor = preferences.edit();

        //Puting the value false for loggedin
        editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);

        //Putting blank value to email
        editor.putString(Config.USER_SHARED_PREF, "");

        //Saving the sharedpreferences
        editor.apply();
        finish();
        //overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        startActivity(new Intent(CallLog.this,SignIn.class));
    }
    public void delete(){
        Log.e("Delete frm db",String.valueOf(db.getRecords().size()-position));
        db.delete(name,date,time);
        list = null;
        try {
            list = db.getRecords();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        listtoshow.remove(position);
        logAdapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        //overridePendingTransition(R.anim.comming_in, R.anim.comming_out);
    }

    /*float x1,x2;
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                x1 = motionEvent.getX();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = motionEvent.getX();
                if (x1 < x2)
                {
                    //Toast.makeText(this, "Left to Right Swap Performed", Toast.LENGTH_LONG).show();
                    NavUtils.navigateUpFromSameTask(this);
                }
                if (x1 > x2)
                {
                    //Toast.makeText(this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

        return false;
    }
    */
}

