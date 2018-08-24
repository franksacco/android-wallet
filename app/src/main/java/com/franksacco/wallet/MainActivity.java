package com.franksacco.wallet;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Main application activity
 */
@SuppressWarnings("RedundantCast")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    /**
     * Active fragment id in drawer navigation
     */
    private int mActiveFragment = R.id.navigationHome;

    private DrawerLayout mDrawerLayout;
    private Menu mDrawerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.mainToolbar);
        this.setSupportActionBar(toolbar);

        this.mDrawerLayout = (DrawerLayout) this.findViewById(R.id.mainLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        this.mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) this.findViewById(R.id.navigationDrawer);
        navigationView.setNavigationItemSelectedListener(this);
        this.mDrawerMenu = navigationView.getMenu();

        this.onNavigationItemSelected(this.mDrawerMenu.findItem(this.mActiveFragment));
        Log.i(TAG, "activity created");
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (this.mActiveFragment != R.id.navigationHome) {
            this.onNavigationItemSelected(this.mDrawerMenu.findItem(R.id.navigationHome));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        item.setChecked(true);

        int id = item.getItemId();
        Fragment fragment = null;
        String title = this.getString(R.string.app_name);

         switch (id) {
            case R.id.navigationHome:
                fragment = new HomeFragment();
                break;
            case R.id.navigationTransactions:
                fragment = new TransactionsFragment();
                title = this.getString(R.string.transactions_title);
                break;
            case R.id.navigationCategories:
                fragment = new CategoriesFragment();
                title = this.getString(R.string.categories_title);
                break;
            case R.id.navigationImport:
                fragment = new ImportFragment();
                title = this.getString(R.string.import_title);
                break;
            case R.id.navigationExport:
                fragment = new ExportFragment();
                title = this.getString(R.string.export_title);
                break;
        }
        this.mActiveFragment = id;
        if (fragment != null) {
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle(title);
        }
        return true;
    }

}
