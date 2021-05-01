package com.projects.mara.msm_caller;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yashasvi on 02-06-2017.
 */

public class ContactsAdapter extends BaseAdapter{

    public List<contact> _data;
    private ArrayList<contact> arraylist;
    Context _c;
    ViewHolder v;
    ButtonListener callListener = null, dropListener = null;

    public ContactsAdapter(List<contact> contacts, Context context) {
        _data = contacts;
        _c = context;
        this.arraylist = new ArrayList<contact>();
        this.arraylist.addAll(_data);
    }

    public interface ButtonListener{
        void onButtonClickListener(int position,String value);
        void onDropClickListener(int position,String value);
    }

    public void setCallButtonListener(ButtonListener listener){
        this.callListener = listener;
    }
    public void setDropButtonListener(ButtonListener listener){
        this.dropListener = listener;
    }

    public String getPhone(int position){
        return _data.get(position).getPhone();
    }
    public String getName(int position){
        return _data.get(position).getName();
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public contact getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    LinearLayout.LayoutParams layoutParams;
    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater li = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.singlecontact, null);
            //Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            //Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.title = (TextView) view.findViewById(R.id.name);
        v.phone = (TextView) view.findViewById(R.id.phno);
        v.call = (FloatingActionButton) view.findViewById(R.id.floatingActionButton4);
        v.drop = (ImageButton) view.findViewById(R.id.imageButton);
        //v.layout = (LinearLayout) view.findViewById(R.id.dropLayout);
        //layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //v.main = (LinearLayout) view.findViewById(R.id.mainLinear);
        v.dp = (ImageView) view.findViewById(R.id.pic);

        final contact data = _data.get(i);
        v.title.setText(data.getName());
        v.phone.setText(data.getPhone());

        try {

            if (data.getThumb() != null) {
                v.dp.setImageBitmap(data.getThumb());
            } else {
                v.dp.setImageResource(R.mipmap.pic);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        v.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callListener!=null){
                    callListener.onButtonClickListener(i,null);
                }
            }
        });

        v.drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dropListener!=null){
                    dropListener.onDropClickListener(i,null);
                }
            }
        });





        view.setTag(data);
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        _data.clear();
        if (charText.length() == 0) {
            _data.addAll(arraylist);
        } else {
            for (contact wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    _data.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
    static class ViewHolder {
        TextView title, phone;
        FloatingActionButton call;
        ImageButton drop;
        ImageView dp;
        //LinearLayout main;
        //TextView text;
    }

}
