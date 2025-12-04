package com.example.capstone_swastik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<RequestModel> requestList;

    public RequestAdapter(List<RequestModel> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestModel request = requestList.get(position);
        holder.tvProductName.setText("Product: " + request.getProductName());
        holder.tvArea.setText("Area: " + request.getAreaInAcres() + " acres");
        holder.tvLocation.setText("Location: " + request.getLocation());
        holder.tvStatus.setText("Status: " + request.getStatus());
        holder.tvGrowthMonths.setText("Growth Months: " + request.getGrowthMonths());
        holder.tvVisitDate.setText("Visit Date: " +
                (request.getVisitDate().isEmpty() ? "Not Assigned" : request.getVisitDate()));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvArea, tvGrowthMonths, tvLocation, tvStatus, tvVisitDate;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
            tvGrowthMonths = itemView.findViewById(R.id.tvGrowthMonths);

        }
    }
}
