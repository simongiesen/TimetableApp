/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.ui.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.timetableapp.R;
import co.timetableapp.ui.agenda.AgendaActivity;
import co.timetableapp.ui.classes.ClassesActivity;
import co.timetableapp.ui.home.MainActivity;
import co.timetableapp.ui.schedule.ScheduleActivity;
import co.timetableapp.ui.settings.SettingsActivity;
import co.timetableapp.ui.subjects.SubjectsActivity;
import co.timetableapp.ui.timetables.TimetablesActivity;

/**
 * An activity used for the navigation drawer implementation.
 *
 * This should be implemented by activities which contain a navigation drawer.
 */
public abstract class NavigationDrawerActivity extends AppCompatActivity {

    protected static final int NAVDRAWER_ITEM_HOME = R.id.navigation_item_home;
    protected static final int NAVDRAWER_ITEM_SCHEDULE = R.id.navigation_item_schedule;
    protected static final int NAVDRAWER_ITEM_AGENDA = R.id.navigation_item_agenda;
    protected static final int NAVDRAWER_ITEM_CLASSES = R.id.navigation_item_classes;
    protected static final int NAVDRAWER_ITEM_SUBJECTS = R.id.navigation_item_subjects;
    protected static final int NAVDRAWER_ITEM_MANAGE_TIMETABLES = R.id.navigation_item_manage_timetables;
    protected static final int NAVDRAWER_ITEM_SETTINGS = R.id.navigation_item_settings;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;

    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerLayout = getSelfDrawerLayout();
        if (mDrawerLayout == null) {
            return;
        }

        setupLayout();
    }

    /**
     * This method should be overridden in subclasses of NavigationDrawerActivity to use the Toolbar
     * Return null if there is no Toolbar
     */
    protected Toolbar getSelfToolbar() {
        return null;
    }

    /**
     * This method should be overridden in subclasses of NavigationDrawerActivity to use the DrawerLayout
     * Return null if there is no DrawerLayout
     */
    protected DrawerLayout getSelfDrawerLayout() {
        return null;
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of NavigationDrawerActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Returns the NavigationView that corresponds to this Activity. Subclasses
     * of NavigationDrawerActivity override this to indicate what nav drawer item corresponds to them
     * Return null to mean that this Activity should not have a Nav Drawer.
     */
    protected NavigationView getSelfNavigationView() {
        return null;
    }

    private void setupLayout() {
        Toolbar toolbar = getSelfToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mNavigationView = getSelfNavigationView();
        if (mNavigationView == null) {
            return;
        }
        mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                handleNavigationSelection(menuItem);
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    private void handleNavigationSelection(final MenuItem menuItem) {
        if (menuItem.getItemId() == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawers();
            return;
        }

        // Launch the target Activity after a short delay, to allow the close animation to play
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(menuItem.getItemId());
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        if (menuItem.isCheckable()) {
            mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(false);
            menuItem.setChecked(true);
        }

        mDrawerLayout.closeDrawers();
    }

    private void goToNavDrawerItem(int menuItem) {
        Intent intent = null;
        switch (menuItem) {
            case NAVDRAWER_ITEM_HOME:
                intent = new Intent(this, MainActivity.class);
                break;
            case NAVDRAWER_ITEM_SCHEDULE:
                intent = new Intent(this, ScheduleActivity.class);
                break;
            case NAVDRAWER_ITEM_AGENDA:
                intent = new Intent(this, AgendaActivity.class);
                break;
            case NAVDRAWER_ITEM_CLASSES:
                intent = new Intent(this, ClassesActivity.class);
                break;
            case NAVDRAWER_ITEM_SUBJECTS:
                intent = new Intent(this, SubjectsActivity.class);
                break;
            case NAVDRAWER_ITEM_MANAGE_TIMETABLES:
                intent = new Intent(this, TimetablesActivity.class);
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                break;
        }

        if (intent == null) {
            return;
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
