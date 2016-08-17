package com.satsumasoftware.timetable.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.AssignmentUtilsKt;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.format.DateTimeFormatter;

public class AssignmentDetailActivity extends AppCompatActivity {

    protected static final String EXTRA_ASSIGNMENT = "extra_assignment";

    protected static final int REQUEST_CODE_ASSIGNMENT_EDIT = 1;

    private Assignment mAssignment;
    private boolean mIsNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            mIsNew = true;
            Intent intent = new Intent(AssignmentDetailActivity.this, AssignmentEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_EDIT);
            return;
        }

        mIsNew = false;
        mAssignment = extras.getParcelable(EXTRA_ASSIGNMENT);

        setupLayouts();
    }

    private void setupLayouts() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChangesAndClose();
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(mAssignment.getTitle());

        Class cls = ClassUtilsKt.getClassWithId(this, mAssignment.getClassId());
        Subject subject = SubjectUtilsKt.getSubjectFromId(this, cls.getSubjectId());
        getSupportActionBar().setSubtitle(subject.getName());

        TextView dateText = (TextView) findViewById(R.id.textView_date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu");
        dateText.setText(mAssignment.getDueDate().format(formatter));

        TextView detailText = (TextView) findViewById(R.id.textView_detail);
        detailText.setText(mAssignment.getDetail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ASSIGNMENT_EDIT) {
            if (resultCode == RESULT_OK) {
                // get the edited assignment (it would have the highest id if new, same id if not)
                mAssignment = AssignmentUtilsKt.getAssignmentWithId(this, mIsNew ?
                        AssignmentUtilsKt.getHighestAssignmentId(this) : mAssignment.getId());

                if (mAssignment == null) {
                    Log.d("ADA!!", "Assignment is NULL");
                    // must have been deleted
                    saveChangesAndClose();
                    return;
                }

                if (mIsNew) {
                    saveChangesAndClose();
                } else {
                    setupLayouts();
                }

            } else if (resultCode == RESULT_CANCELED) {
                if (mIsNew) {
                    cancelAndClose();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(AssignmentDetailActivity.this, AssignmentEditActivity.class);
                intent.putExtra(AssignmentEditActivity.EXTRA_ASSIGNMENT, mAssignment);
                startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_EDIT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveChangesAndClose();
        super.onBackPressed();
    }

    private void cancelAndClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveChangesAndClose() {
        setResult(RESULT_OK); // to reload any changes in AssignmentsActivity
        finish();
    }

}