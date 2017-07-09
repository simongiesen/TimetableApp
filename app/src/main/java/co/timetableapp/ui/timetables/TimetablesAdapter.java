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

package co.timetableapp.ui.timetables;

import android.app.Activity;
import android.app.Application;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.model.Timetable;

class TimetablesAdapter extends RecyclerView.Adapter<TimetablesAdapter.TimetablesViewHolder> {

    private Activity mActivity;
    private Application mApplication;
    private ArrayList<Timetable> mTimetables;
    private View mRootView;

    private boolean mBindingVH;

    public TimetablesAdapter(Activity activity, ArrayList<Timetable> timetables, View rootView) {
        mActivity = activity;
        mApplication = activity.getApplication();
        mTimetables = timetables;
        mRootView = rootView;
    }

    @Override
    public TimetablesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new TimetablesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimetablesViewHolder holder, int position) {
        mBindingVH = true;

        Timetable timetable = mTimetables.get(position);

        holder.mName.setText(timetable.getDisplayedName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uu");
        String detailText = timetable.getStartDate().format(formatter) + " - " +
                timetable.getEndDate().format(formatter);
        holder.mDetails.setText(detailText);

        Timetable currentTimetable = ((TimetableApplication) mApplication).getCurrentTimetable();
        assert currentTimetable != null;
        boolean isCurrent = currentTimetable.getId() == timetable.getId();

        holder.mRadioButton.setChecked(isCurrent);

        mBindingVH = false;
    }

    @Override
    public int getItemCount() {
        return mTimetables.size();
    }

    class TimetablesViewHolder extends RecyclerView.ViewHolder {

        RadioButton mRadioButton;
        TextView mName, mDetails;

        TimetablesViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnEntryClickListener != null) {
                        mOnEntryClickListener.onEntryClick(view, getLayoutPosition());
                    }
                }
            });

            mRadioButton = (RadioButton) itemView.findViewById(R.id.radioButton);
            mRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!mBindingVH) {
                        Timetable timetable = mTimetables.get(getLayoutPosition());
                        TimetableApplication application = (TimetableApplication) mApplication;
                        application.setCurrentTimetable(mActivity, timetable);
                        notifyDataSetChanged();

                        String snackbarText = mActivity.getString(
                                R.string.message_set_current_timetable,
                                timetable.getDisplayedName());
                        Snackbar.make(mRootView, snackbarText, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

            mName = (TextView) itemView.findViewById(R.id.name);
            mDetails = (TextView) itemView.findViewById(R.id.details);
        }
    }

    private OnEntryClickListener mOnEntryClickListener;

    public interface OnEntryClickListener {
        void onEntryClick(View view, int position);
    }

    public void setOnEntryClickListener(OnEntryClickListener onEntryClickListener) {
        mOnEntryClickListener = onEntryClickListener;
    }

}
