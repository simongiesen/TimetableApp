package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.TimetableUtils;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.TimetablesAdapter;
import com.satsumasoftware.timetable.util.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TimetablesActivity extends BaseActivity {

    protected static final int REQUEST_CODE_TIMETABLE_EDIT = 1;

    private ArrayList<Timetable> mTimetables;
    private TimetablesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTimetables = TimetableUtils.getTimetables(this);
        sortList();

        mAdapter = new TimetablesAdapter(this, mTimetables, findViewById(R.id.coordinatorLayout));
        mAdapter.setOnEntryClickListener(new TimetablesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                intent.putExtra(TimetableEditActivity.EXTRA_TIMETABLE, mTimetables.get(position));

                Bundle bundle = null;
                if (ThemeUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    TimetablesActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        TimetablesActivity.this, intent, REQUEST_CODE_TIMETABLE_EDIT, bundle);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT);
            }
        });
    }

    private void refreshList() {
        mTimetables.clear();
        mTimetables.addAll(TimetableUtils.getTimetables(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mTimetables, new Comparator<Timetable>() {
            @Override
            public int compare(Timetable t1, Timetable t2) {
                return t1.getStartDate().compareTo(t2.getStartDate());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    protected Toolbar getSelfToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected DrawerLayout getSelfDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MANAGE_TIMETABLES;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
