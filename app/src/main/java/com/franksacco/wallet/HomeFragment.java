package com.franksacco.wallet;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


@SuppressWarnings("RedundantCast")
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fab_add_transaction =
                (FloatingActionButton) view.findViewById(R.id.fab_add_transaction);
        fab_add_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(),
                        AddTransactionActivity.class));
            }
        });

        setupCardsValues(view);
        return view;
    }

    /**
     * Populate cards in view with values
     */
    private void setupCardsValues(View view) {
        TextView balance = (TextView) view.findViewById(R.id.balance);
        //balance.setText("0.00 €");
        TextView balance_in = (TextView) view.findViewById(R.id.balance_in);
        //balance_in.setText("+0.00 €");
        TextView balance_out = (TextView) view.findViewById(R.id.balance_out);
        //balance_out.setText("-0.00 €");

        // todo set expense statistics
        TextView expense_today = (TextView) view.findViewById(R.id.expense_today);
        TextView expense_yesterday = (TextView) view.findViewById(R.id.expense_yesterday);
        TextView expense_this_week = (TextView) view.findViewById(R.id.expense_this_week);
        TextView expense_this_month = (TextView) view.findViewById(R.id.expense_this_month);
    }

}
