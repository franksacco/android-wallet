package com.franksacco.wallet.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franksacco.wallet.R;
import com.franksacco.wallet.entities.Category;

import java.util.ArrayList;


/**
 * Adapter for categories recycler view
 */
@SuppressWarnings("RedundantCast")
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    /**
     * Dataset
     */
    private ArrayList<Category> mDataset = new ArrayList<>();

    /**
     * Category manager
     */
    private OnActionClickListener mActionClickListener;

    /**
     * Adapter constructor
     */
    public CategoriesAdapter(OnActionClickListener categoriesManager) {
        this.setHasStableIds(true);
        this.mActionClickListener = categoriesManager;
    }

    /**
     * Set items of dataset
     * @param items Dataset
     */
    public void setItems(ArrayList<Category> items) {
        this.mDataset.clear();
        this.mDataset.addAll(items);
        this.notifyDataSetChanged();
    }

    /**
     * Add item to dataset
     * @param item Item to be added
     */
    public void addItem(Category item) {
        int position = getItemCount();
        this.mDataset.add(item);
        this.notifyItemInserted(position);
    }

    /**
     * Remove an item from dataset
     * @param position Position of the item to be removed
     */
    public void removeItem(int position) {
        this.mDataset.remove(position);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_one_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Category category = this.mDataset.get(position);
        holder.setIcon(category.getIconIdentifier());
        holder.setTitle(category.getName());
        holder.setActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoriesAdapter.this.mActionClickListener
                        .OnActionClick(category, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.mDataset.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mTitle;
        private ImageView mAction;

        ViewHolder(LinearLayout itemView) {
            super(itemView);
            this.mIcon = (ImageView) itemView.findViewById(R.id.itemOneLine_icon);
            this.mTitle = (TextView) itemView.findViewById(R.id.itemOneLine_title);
            this.mAction = (ImageView) itemView.findViewById(R.id.itemOneLine_action);
            this.mAction.setImageResource(R.drawable.ic_delete_black_24dp);
        }

        public void setIcon(int resourceId) {
            this.mIcon.setImageResource(resourceId);
        }

        public void setTitle(String title) {
            this.mTitle.setText(title);
        }

        void setActionClickListener(View.OnClickListener listener) {
            this.mAction.setOnClickListener(listener);
        }

    }

    /**
     * Interface for manage action click
     */
    public interface OnActionClickListener {

        /**
         * Method called on action view click
         * @param category Category object associated to row clicked
         * @param position Item position
         */
        void OnActionClick(Category category, int position);

    }

}
