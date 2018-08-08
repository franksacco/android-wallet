package com.franksacco.wallet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


/**
 * Main application activity
 */
@SuppressWarnings("RedundantCast")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;

    private boolean viewIsAtHome = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);

        displayView(R.id.navigation_home);

        Log.d(TAG, "activity created");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!viewIsAtHome) {
            displayView(R.id.navigation_home);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        displayView(item.getItemId());
        return true;
    }

    /**
     * Replace actual content fragment with another
     * @param id Item identifier
     */
    public void displayView(int id) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        viewIsAtHome = false;
        switch (id) {
            case R.id.navigation_home:
                viewIsAtHome = true;
                fragment = new HomeFragment();
                break;
            case R.id.navigation_transactions:

                break;
            case R.id.navigation_categories:
                fragment = new CategoriesFragment();
                title = getResources().getString(R.string.categories_title);
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
        }

        if (fragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_main, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

}
