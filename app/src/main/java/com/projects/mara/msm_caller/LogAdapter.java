package com.projects.mara.msm_caller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Yashasvi on 02-06-2017.
 */

public class LogAdapter extends ArrayAdapter<logdetails>{

    private List<logdetails> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView Name;
        TextView Time;
        TextView Date,Balance;
    }

    public LogAdapter(List<logdetails> data, Context context) {
        super(context, R.layout.singlelog, data);
        this.dataSet = data;
        this.mContext=context;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        logdetails dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.singlelog, parent, false);
            viewHolder.Name = (TextView) convertView.findViewById(R.id.textView2);
            viewHolder.Time = (TextView) convertView.findViewById(R.id.textView3);
            viewHolder.Date = (TextView) convertView.findViewById(R.id.textView4);
            viewHolder.Balance = (TextView) convertView.findViewById(R.id.balance);
            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.Name.setText(dataModel.getName());
        viewHolder.Time.setText(dataModel.getCallTime());
        viewHolder.Date.setText(dataModel.getDate());
        viewHolder.Balance.setText(dataModel.getBalance());
        // Return the completed view to render on screen
        return convertView;
    }
}