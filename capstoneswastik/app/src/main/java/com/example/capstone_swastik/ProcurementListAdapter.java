package com.example.capstone_swastik;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProcurementListAdapter extends RecyclerView.Adapter<ProcurementListAdapter.ViewHolder> {

    ArrayList<Procurement> list;
    Context context;

    public ProcurementListAdapter(ArrayList<Procurement> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_procurement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Procurement p = list.get(position);

        final String readableDate;
        if (p.getTimestamp() != 0) {
            Date dateObj = new Date(p.getTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            readableDate = sdf.format(dateObj);
        } else {
            readableDate = "N/A";
        }

        h.name.setText(p.getSupplierName());
        h.billNumber.setText("Bill: " + (p.getBillNumber() != null ? p.getBillNumber() : "N/A"));
        h.qty.setText("Qty: " + p.getQuantity());
        h.amount.setText("Total: ₹" + p.getTotalAmount());
        h.status.setText("Status: " + p.getStatus());
        h.date.setText("Date: " + readableDate);

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProcurementDetailsActivity.class);
            i.putExtra("supplierName", p.getSupplierName());
            i.putExtra("date", readableDate);
            i.putExtra("status", p.getStatus());
            i.putExtra("quantity", String.valueOf(p.getQuantity()));
            i.putExtra("pricePerUnit", String.valueOf(p.getPricePerUnit()));
            i.putExtra("totalAmount", String.valueOf(p.getTotalAmount()));
            i.putExtra("paymentDate", p.getPaymentDate());
            i.putExtra("billNumber", p.getBillNumber()); // pass bill number
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, billNumber, qty, amount, status, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rowSupplierName);
            billNumber = itemView.findViewById(R.id.rowBillNumber);
            qty = itemView.findViewById(R.id.rowQty);
            amount = itemView.findViewById(R.id.rowAmount);
            status = itemView.findViewById(R.id.rowStatus);
            date = itemView.findViewById(R.id.rowDate);
        }
    }
}
