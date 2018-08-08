package com.franksacco.wallet.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franksacco.wallet.R;
import com.franksacco.wallet.entities.Category;

import java.util.ArrayList;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private ArrayList<Category> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mView;

        ViewHolder(LinearLayout v) {
            super(v);
            mView = v;
        }

        public void setIcon(int resourceId) {
            CardView iconContainer = (CardView) mView.getChildAt(0);
            ImageView icon = (ImageView) iconContainer.getChildAt(0);
            icon.setImageResource(resourceId);
        }

        public void setTitle(String title) {
            TextView textView = (TextView) mView.getChildAt(1);
            textView.setText(title);
        }
    }

    public CategoriesAdapter(ArrayList<Category> dataset) {
        mDataset = dataset;
    }

    /**
     * Add item to dataset
     * @param item Item to be added
     */
    public void addItem(Category item) {
        int position = getItemCount();
        mDataset.add(item);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_one_line, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = mDataset.get(position);
        holder.setIcon(category.getIconIdentifier());
        holder.setTitle(category.getName());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
