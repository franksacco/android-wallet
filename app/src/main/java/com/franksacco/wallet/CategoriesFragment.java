package com.franksacco.wallet;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franksacco.wallet.adapters.CategoriesAdapter;
import com.franksacco.wallet.entities.Category;
import com.franksacco.wallet.database.CategoryOpenHelper;

import java.util.ArrayList;


@SuppressWarnings("RedundantCast")
public class CategoriesFragment extends Fragment
        implements AddCategoryDialog.AddCategoryDialogListener {

    private static final String TAG = "CategoriesFragment";

    private CategoriesAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "creating view...");

        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.categories_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        CategoryOpenHelper categoryHelper =
                new CategoryOpenHelper(getActivity().getApplicationContext());
        ArrayList<Category> list = categoryHelper.select(CategoryOpenHelper.ALL_COLUMNS,
                null, null, null, null, null, null);

        mAdapter = new CategoriesAdapter(list);
        recyclerView.setAdapter(mAdapter);

        bindAddCategoryFab(view);

        return view;
    }

    /**
     * Bind onClick action for 'addCategory' floating action button
     * @param view Fragment view
     */
    private void bindAddCategoryFab(View view) {
        FloatingActionButton fab_add_category =
                (FloatingActionButton) view.findViewById(R.id.fab_add_category);
        fab_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new AddCategoryDialog();
                dialog.setTargetFragment(CategoriesFragment.this, 1);
                dialog.show(getFragmentManager(), "add_category");
            }
        });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String inputName) {
        String name = inputName.trim();
        if (!name.isEmpty()) {
            addCategory(name);
        }
    }

    /**
     * Insert category in database, notify recycler view and show snackbar
     * @param name New category name
     */
    private void addCategory(String name) {
        CategoryOpenHelper databaseHelper = new CategoryOpenHelper(getActivity());
        Category category = new Category(0, "ic_style_white_24dp", name);

        int messageId = R.string.addCategory_ok;
        if (databaseHelper.insert(category) == -1) {
            messageId = R.string.addCategory_error;
        } else {
            mAdapter.addItem(category);
        }
        Snackbar.make(getActivity().findViewById(R.id.fragment_categories),
                messageId, Snackbar.LENGTH_SHORT)
                .show();
    }

}
