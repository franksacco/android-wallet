package com.franksacco.wallet;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.AsyncTask;
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
import com.franksacco.wallet.helpers.CategoriesManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Category list fragment view
 */
@SuppressWarnings("RedundantCast")
public class CategoriesFragment extends Fragment
        implements AddCategoryDialog.AddCategoryDialogListener {

    private static final String TAG = "CategoriesFragment";

    private CategoriesAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.categories_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.categoriesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        this.mAdapter = new CategoriesAdapter();
        recyclerView.setAdapter(this.mAdapter);

        new LoadCategories(this).execute();

        this.bindAddCategoryFab(view);

        Log.d(TAG, "view created");
        return view;
    }

    /**
     * Bind onClick action for 'addCategory' floating action button
     * @param view Fragment view
     */
    private void bindAddCategoryFab(View view) {
        FloatingActionButton fab_add_category =
                (FloatingActionButton) view.findViewById(R.id.addCategoryFab);
        fab_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new AddCategoryDialog();
                dialog.setTargetFragment(CategoriesFragment.this, 1);
                dialog.show(getFragmentManager(), "addCategory");
            }
        });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String inputName) {
        String name = inputName.trim();
        if (!name.isEmpty()) {
            this.addCategory(name);
        }
    }

    /**
     * Insert category in database, notify recycler view and show snackbar
     * @param name New category name
     */
    private void addCategory(String name) {
        CategoriesManager databaseHelper = new CategoriesManager(getActivity());
        Category category = new Category("ic_style_white_24dp", name);

        int messageId = R.string.addCategory_ok;
        if (databaseHelper.insert(category) == -1) {
            messageId = R.string.addCategory_error;
        } else {
            this.mAdapter.addItem(category);
        }
        Snackbar.make(getActivity().findViewById(R.id.categoriesLayout),
                messageId, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Asynchronous categories loading
     */
    private static class LoadCategories extends AsyncTask<Void, Void, ArrayList<Category>> {

        private WeakReference<CategoriesFragment> mReference;

        LoadCategories(CategoriesFragment context) {
            this.mReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Category> doInBackground(Void... voids) {
            CategoriesFragment fragment = this.mReference.get();
            if (fragment == null) return null;
            Activity activity = fragment.getActivity();
            if (activity.isFinishing()) return null;

            CategoriesManager manager = new CategoriesManager(activity.getApplicationContext());
            return manager.select(CategoriesManager.ALL_COLUMNS, null,
                    null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(ArrayList<Category> categories) {
            CategoriesFragment fragment = this.mReference.get();
            if (fragment != null) {
                fragment.mAdapter.setItems(categories);
            }
        }

    }

}
