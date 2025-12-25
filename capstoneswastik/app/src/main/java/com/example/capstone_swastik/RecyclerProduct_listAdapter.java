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
        ProductModel product = arrProducts.get(position);

        holder.imgProduct.setImageResource(product.img);
        holder.textProdcutName.setText(product.displayName);   // use displayName
        holder.textProductPrice.setText(product.displayPrice); // use displayPrice

        holder.btnBuy.setOnClickListener(v -> {
            Intent intent = new Intent(context, Product_details.class);

            intent.putExtra("name", product.rawName);
            intent.putExtra("price", product.rawPrice);
            intent.putExtra("img", product.img);
            intent.putExtra("docId", product.id);
            intent.putExtra("docId", product.id);
            intent.putExtra("description", product.description);

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
