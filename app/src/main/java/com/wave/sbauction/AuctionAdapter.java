package com.wave.sbauction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class AuctionAdapter extends RecyclerView.Adapter<AuctionAdapter.ViewHolder> {
    List<Auction> auctions;

    public AuctionAdapter(List<Auction> auctions) {
        this.auctions = auctions;
    }

    @NonNull
    @Override
    public AuctionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auction_row, parent,  false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionAdapter.ViewHolder holder, int position) {
        holder.item_name.setText(auctions.get(position).item_name);
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
        }
    }
}
