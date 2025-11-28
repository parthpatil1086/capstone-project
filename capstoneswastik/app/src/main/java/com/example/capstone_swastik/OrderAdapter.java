package com.example.capstone_swastik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    ArrayList<OrderModel> list;
    OnOrderClick listener;

    public interface OnOrderClick {
        void onClick(OrderModel order);
    }

    public OrderAdapter(ArrayList<OrderModel> list, OnOrderClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        OrderModel m = list.get(i);

        h.tvName.setText(m.productName);
        h.tvTotal.setText("Total: ₹ " + m.totalAmount);
        h.tvDate.setText(m.date);
        h.tvStatus.setText("Status: " + m.orderStatus);
        h.img.setImageResource(m.img);

        h.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvTotal, tvDate, tvStatus;

        public ViewHolder(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvProductName);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvDate = v.findViewById(R.id.tvDate);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
