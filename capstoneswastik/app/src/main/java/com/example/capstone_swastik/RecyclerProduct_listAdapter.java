package com.example.capstone_swastik;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        // Button click listener
        holder.btnBuy.setOnClickListener(v -> {
            Intent intent = new Intent(context, Product_details.class);
            intent.putExtra("name", arrProducts.get(position).p_name);
            intent.putExtra("price", arrProducts.get(position).p_price);
            intent.putExtra("img", arrProducts.get(position).img);
            intent.putExtra("docId", arrProducts.get(position).id);
            intent.putExtra("description", arrProducts.get(position).description);
            // Required if context is not an Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProdcutName,textProductPrice;
        ImageView imgProduct;
        Button btnBuy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProdcutName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}
