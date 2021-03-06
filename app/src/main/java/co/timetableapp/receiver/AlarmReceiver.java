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

package co.timetableapp.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.threeten.bp.LocalDate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.data.handler.AssignmentHandler;
import co.timetableapp.data.handler.DataNotFoundException;
import co.timetableapp.model.Assignment;
import co.timetableapp.model.Class;
import co.timetableapp.model.ClassDetail;
import co.timetableapp.model.ClassTime;
import co.timetableapp.model.Color;
import co.timetableapp.model.Event;
import co.timetableapp.model.Exam;
import co.timetableapp.model.Subject;
import co.timetableapp.ui.agenda.AgendaActivity;
import co.timetableapp.ui.home.MainActivity;

/**
 * Invoked when receiving an alarm - i.e. when a notification needs to be displayed.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String LOG_TAG = "AlarmReceiver";

    private static final long NO_REPEAT_INTERVAL = -1;

    /**
     * The vibration pattern used for all notifications.
     *
     * The first value (0) indicates 0 ms delay before the start of the vibration. The vibration
     * itself will be a single 'buzz' lasting 200 ms.
     *
     * @see android.os.Vibrator#vibrate(long[], int)
     */
    private static final long[] VIBRATION_PATTERN = {0, 200};

    private static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final String EXTRA_NOTIFICATION_TYPE = "extra_notification_type";

    /**
     * The identifier used for showing the notification for assignments due the following day.
     *
     * There is only one identifier because the user gets one (repeated) notification that shows
     * all assignments due the following day.
     *
     * @see #ASSIGNMENTS_OVERDUE_NOTIFICATION_ID
     */
    public static final int ASSIGNMENTS_NOTIFICATION_ID = 1;

    /**
     * The identifier used for showing the notification for overdue assignments.
     *
     * There is only one identifier because the user gets one (repeated) notification that shows
     * all assignments that are overdue.
     *
     * @see #ASSIGNMENTS_NOTIFICATION_ID
     */
    public static final int ASSIGNMENTS_OVERDUE_NOTIFICATION_ID = 2;

    /**
     * Represents the different possible categories of notification in this app: notifications for
     * a class, assignment (upcoming and overdue), exam, or event.
     *
     * We use this to display notifications differently for each of the categories.
     */
    @IntDef({Type.CLASS, Type.ASSIGNMENT, Type.ASSIGNMENT_OVERDUE, Type.EXAM, Type.EVENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int CLASS = 1;
        int ASSIGNMENT = 2;
        int ASSIGNMENT_OVERDUE = 3;
        int EXAM = 4;
        int EVENT = 5;
    }

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent data) {
        Bundle extras = data.getExtras();
        int id = extras.getInt(EXTRA_ITEM_ID);
        @Type int notificationType = extras.getInt(EXTRA_NOTIFICATION_TYPE);

        int notificationId = makeNotificationId(notificationType, id);

        Color color = new Color(7); // default color is light blue
        Intent intent;

        String contentTitle, contentText, tickerText;
        @DrawableRes int drawableRes;

        switch (notificationType) {
            case Type.CLASS:
                ClassTime classTime = null;
                ClassDetail classDetail = null;
                Class cls = null;
                Subject classSubject = null;
                try {
                    classTime = ClassTime.create(context, id);
                    classDetail = ClassDetail.create(context, classTime.getClassDetailId());
                    cls = Class.create(context, classDetail.getClassId());
                    classSubject = Subject.create(context, cls.getSubjectId());
                } catch (DataNotFoundException e) {
                    e.printStackTrace();
                }

                assert classTime != null;
                assert classDetail != null;
                assert cls != null;
                assert classSubject != null;

                color = new Color(classSubject.getColorId());
                intent = new Intent(context, MainActivity.class);

                contentTitle = classSubject.getName();
                contentText = makeClassText(classDetail, classTime);
                tickerText = classSubject.getName() + " class starting in 5 minutes";
                drawableRes = R.drawable.ic_class_notification;
                break;

            case Type.ASSIGNMENT:
            case Type.ASSIGNMENT_OVERDUE:
                boolean checkingOverdue = notificationType == Type.ASSIGNMENT_OVERDUE;

                ArrayList<Assignment> assignments = new AssignmentHandler(context)
                        .getItems((TimetableApplication) context.getApplicationContext());

                int count = 0;

                for (Assignment assignment : assignments) {
                    boolean addToCount;
                    if (checkingOverdue) {
                        addToCount = assignment.isOverdue();
                    } else {
                        addToCount = !assignment.isComplete() &&
                                assignment.getDueDate().minusDays(1).equals(LocalDate.now());
                    }

                    if (addToCount) {
                        count++;
                    }
                }

                if (count == 0) {
                    return;
                }

                intent = new Intent(context, AgendaActivity.class);

                int pluralRes = checkingOverdue
                        ? R.plurals.notification_overdue_assignments
                        : R.plurals.notification_incomplete_assignments;

                contentTitle = context.getResources().getQuantityString(
                        pluralRes,
                        count,
                        count);
                contentText = "";
                tickerText = contentTitle;
                drawableRes = R.drawable.ic_homework_notification;
                break;

            case Type.EXAM:
                Exam exam = null;
                Subject examSubject = null;
                try {
                    exam = Exam.create(context, id);
                    examSubject = Subject.create(context, exam.getSubjectId());
                } catch (DataNotFoundException e) {
                    e.printStackTrace();
                }

                assert exam != null;
                assert examSubject != null;

                color = new Color(examSubject.getColorId());
                intent = new Intent(context, AgendaActivity.class);

                contentTitle = examSubject.getName() + ": " + exam.getModuleName() + " exam";
                contentText = makeExamText(exam);
                tickerText = examSubject.getName() + " exam starting in 30 minutes";
                drawableRes = R.drawable.ic_assessment_notification;
                break;

            case Type.EVENT:
                Event event = null;
                try {
                    event = Event.create(context, id);
                } catch (DataNotFoundException e) {
                    e.printStackTrace();
                }
                assert event != null;

                color = new Color(19);
                intent = new Intent(context, AgendaActivity.class);

                contentTitle = event.getTitle();
                contentText = event.getStartDateTime().toLocalTime() + " - " +
                        event.getEndDateTime().toLocalTime();
                tickerText = event.getTitle() + " starting in 30 minutes";
                drawableRes = R.drawable.ic_event_notification;
                // TODO create string resource for this depending on customizable reminder time
                break;

            default:
                throw new IllegalArgumentException("invalid notification type");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String hexString = Integer.toHexString(
                ContextCompat.getColor(context, color.getPrimaryColorResId(context)));
        int colorArgb = android.graphics.Color.parseColor("#" + hexString);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setSmallIcon(drawableRes)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(colorArgb)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setTicker(tickerText)
                .setVibrate(VIBRATION_PATTERN);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public void setAlarm(Context context, @Type int notificationType, Calendar dateTime,
                         int itemId) {
        setRepeatingAlarm(context, notificationType, dateTime, itemId, NO_REPEAT_INTERVAL);
    }

    public void setRepeatingAlarm(Context context, @Type int notificationType,
                                  Calendar startDateTime, int itemId, long repeatInterval) {
        boolean isRepeat = repeatInterval != NO_REPEAT_INTERVAL;
        Log.d(LOG_TAG, isRepeat ?
                "Setting repeating alarm for calendar: " + startDateTime.toString() :
                "Setting alarm for calendar: " + startDateTime.toString());

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, notificationType);

        mPendingIntent = PendingIntent.getBroadcast(context,
                makeNotificationId(notificationType, itemId),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification time
        long startTimeMs = startDateTime.getTimeInMillis();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();
        long diffTime = startTimeMs - currentTimeMs;

        // Start alarm(s) using notification time
        if (isRepeat) {
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + diffTime,
                    repeatInterval,
                    mPendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + diffTime,
                    mPendingIntent);
        }

        // Restart alarm if device is rebooted
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context, @Type int notificationType, int itemId) {
        Log.d(LOG_TAG, "Cancelling repeated alarm for an item with id: " + itemId);

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel alarm using id
        Intent intent = new Intent(context, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context,
                makeNotificationId(notificationType, itemId),
                intent,
                0);
        mAlarmManager.cancel(mPendingIntent);

        // Disable alarm
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private int makeNotificationId(@Type int notificationType, int itemId) {
        return (notificationType * 100000) + itemId;
    }

    private String makeClassText(ClassDetail classDetail, ClassTime classTime) {
        StringBuilder builder = new StringBuilder();

        builder.append(classTime.getStartTime().toString())
                .append(" - ")
                .append(classTime.getEndTime().toString());

        if (classDetail.hasRoom() || classDetail.hasBuilding()) {
            builder.append(" \u2022 ");

            if (classDetail.hasRoom()) {
                builder.append(classDetail.getRoom());
                if (classDetail.hasBuilding()) builder.append(", ");
            }
            if (classDetail.hasBuilding()) {
                builder.append(classDetail.getBuilding());
            }
        }

        return builder.toString();
    }

    private String makeExamText(Exam exam) {
        StringBuilder builder = new StringBuilder();

        builder.append(exam.getStartTime());

        if (exam.hasSeat() || exam.hasRoom()) {
            builder.append(" \u2022 ");

            if (exam.hasSeat()) {
                builder.append(exam.getSeat());
                if (exam.hasRoom()) builder.append(", ");
            }
            if (exam.hasRoom()) {
                builder.append(exam.getRoom());
            }
        }

        return builder.toString();
    }

}
