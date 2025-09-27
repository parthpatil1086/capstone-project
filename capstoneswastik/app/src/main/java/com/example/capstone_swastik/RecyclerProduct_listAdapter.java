package com.example.capstone_swastik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerProduct_listAdapter extends RecyclerView.Adapter<RecyclerProduct_listAdapter.ViewHolder> {
    Context context;
    ArrayList<ProductModel> arrProducts;

    RecyclerProduct_listAdapter(Context context, ArrayList<ProductModel> arrProducts){
        this.context = context;
        this.arrProducts = arrProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.product_row,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imgProduct.setImageResource(arrProducts.get(position).img);
        holder.textProdcutName.setText(arrProducts.get(position).p_name);
        holder.textProductPrice.setText((arrProducts.get(position).p_price));
    }

    @Override
    public int getItemCount() {
        return arrProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProdcutName,textProductPrice;
        ImageView imgProduct;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProdcutName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}
