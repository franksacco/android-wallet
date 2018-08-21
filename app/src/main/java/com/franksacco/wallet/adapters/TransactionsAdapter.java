package com.franksacco.wallet.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franksacco.wallet.R;
import com.franksacco.wallet.entities.Transaction;
import com.franksacco.wallet.helpers.CurrencyManager;

import java.util.ArrayList;
import java.util.Locale;


/**
 * Adapter for transactions recycler view
 */
@SuppressWarnings("RedundantCast")
public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    /**
     * Dataset
     */
    private ArrayList<Transaction> mDataset = new ArrayList<>();
    /**
     * Application context
     */
    private Context mContext;
    /**
     * Currency manager instance
     */
    private CurrencyManager mCurrencyManager;
    /**
     * Optional on click listener
     */
    private OnItemClickListener mListener;

    /**
     * Adapter constructor
     * @param context Application context
     * @param listener Optional on click listener
     */
    public TransactionsAdapter(Context context, @Nullable OnItemClickListener listener) {
        this.mContext = context;
        this.mCurrencyManager = new CurrencyManager(context);
        this.mListener = listener;
        this.setHasStableIds(true);
    }

    /**
     * Set items of dataset
     * @param items Dataset
     */
    public void setItems(ArrayList<Transaction> items) {
        this.mDataset.clear();
        this.mDataset.addAll(items);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_two_lines, parent, false);
        return new ViewHolder(view, this.mListener);
    }

    @Override
    public long getItemId(int position) {
        return this.mDataset.get(position).getId();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = this.mDataset.get(position);

        holder.setTitle(t.getCategory().getName() + " (" + t.getPaymentType(this.mContext) + ")");
        holder.setSubtitle(t.getNotes());
        holder.setIcon(t.getCategory().getIconIdentifier());
        holder.setMeta(DateFormat.format("HH:mm", t.getDateTime()).toString());
        double amount = this.mCurrencyManager.convertToPreferred(t.getAmount());
        holder.setSecondaryText(
                String.format(Locale.getDefault(), "%+.2f", amount)
                        + this.mCurrencyManager.getPreferredSymbol(),
                t.getAmount() < 0 ? Color.parseColor("#f44336")
                        : Color.parseColor("#4caf50"));
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
        private TextView mSubtitle;
        private TextView mMeta;
        private TextView mSecondaryText;

        ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            this.mIcon = (ImageView) itemView.findViewById(R.id.itemTwoLines_icon);
            this.mTitle = (TextView) itemView.findViewById(R.id.itemTwoLines_title);
            this.mSubtitle = (TextView) itemView.findViewById(R.id.itemTwoLines_subtitle);
            this.mMeta = (TextView) itemView.findViewById(R.id.itemTwoLines_meta);
            this.mSecondaryText = (TextView) itemView.findViewById(R.id.itemTwoLines_secondaryText);

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(ViewHolder.this);
                    }
                });
            }
        }

        void setIcon(int resourceId) {
            this.mIcon.setImageResource(resourceId);
        }

        void setTitle(String title) {
            this.mTitle.setText(title);
        }

        void setSubtitle(String secondaryText) {
            this.mSubtitle.setText(secondaryText);
        }

        void setMeta(String meta) {
            this.mMeta.setText(meta);
        }

        void setSecondaryText(String secondaryText, int textColor) {
            this.mSecondaryText.setText(secondaryText);
            this.mSecondaryText.setTextColor(textColor);
        }

    }

    /**
     * Interface to be implemented in order to perform an item click
     */
    public interface OnItemClickListener {
        /**
         * Method called when user click on item
         * @param item View holder
         */
        void onItemClick(ViewHolder item);
    }

}
