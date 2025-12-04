package com.example.capstone_swastik;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class NotificationAdapter extends BaseAdapter {

    private Context context;
    private List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.notification_list_item, parent, false);
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvBody = view.findViewById(R.id.tvBody);
        TextView tvTime = view.findViewById(R.id.tvTime);

        NotificationModel model = notificationList.get(position);

        tvTitle.setText(model.getTitle());
        tvBody.setText(model.getBody());
        tvTime.setText(model.getTime());

        // Force dark color for all text
        tvTitle.setTextColor(Color.parseColor("#000000"));
        tvBody.setTextColor(Color.parseColor("#333333"));
        tvTime.setTextColor(Color.parseColor("#555555"));

        return view;
    }
}
