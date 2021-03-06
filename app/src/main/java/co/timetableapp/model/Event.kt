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

package co.timetableapp.model

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.R
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.EventsSchema
import co.timetableapp.model.agenda.AgendaItem
import co.timetableapp.model.home.HomeItem
import co.timetableapp.model.home.HomeItemProperties
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an event that is not part of the usual schedule of the user.
 * For example, a meeting with a tutor, a sporting event, etc.
 *
 * Some events may be related to subjects; for example, meetings with tutors of particular subjects.
 * This is represented by the [relatedSubjectId] property.
 *
 * @property title              this event's title
 * @property notes              additional notes and details for this event
 * @property startDateTime      the starting time and date
 * @property endDateTime        the ending time and date
 * @property location           some text for the location of this event
 * @property relatedSubjectId   the identifier for the related subject to this event. If this event
 *                              has no related subject, the value of this property should be 0.
 */
data class Event(
        override val id: Int,
        override val timetableId: Int,
        val title: String,
        val notes: String,
        val startDateTime: LocalDateTime,
        val endDateTime: LocalDateTime,
        val location: String,
        val relatedSubjectId: Int
) : TimetableItem, AgendaItem, HomeItem {

    companion object {

        /**
         * The default color to use when displaying events in lists.
         * Note that the UI could be affected by the [relatedSubjectId].
         */
        @JvmField val DEFAULT_COLOR = Color(19) // blue-grey

        /**
         * Constructs an Event using column values from the cursor provided
         *
         * @param cursor a query of the events table
         * @see [EventsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Event {
            val startDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_DAY_OF_MONTH)))
            val startTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_TIME_MINS)))

            val endDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_DAY_OF_MONTH)))
            val endTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_TIME_MINS)))

            return Event(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_TIMETABLE_ID)),
                    cursor.getString(cursor.getColumnIndex(EventsSchema.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(EventsSchema.COL_DETAIL)),
                    LocalDateTime.of(startDate, startTime),
                    LocalDateTime.of(endDate, endTime),
                    cursor.getString(cursor.getColumnIndex(EventsSchema.COL_LOCATION)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_RELATED_SUBJECT_ID)))
        }

        /**
         * Creates an [Event] from the [eventId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, eventId: Int): Event {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    EventsSchema.TABLE_NAME,
                    null,
                    "${EventsSchema._ID}=?",
                    arrayOf(eventId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, eventId)
            }

            cursor.moveToFirst()
            val event = from(cursor)
            cursor.close()
            return event
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }

    private constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDateTime,
            source.readSerializable() as LocalDateTime,
            source.readString(),
            source.readInt()
    )

    fun hasDifferentStartEndDates() = startDateTime.toLocalDate() != endDateTime.toLocalDate()

    fun hasNotes() = notes.isNotEmpty()
  
    fun hasLocation() = location.isNotEmpty()

    fun hasRelatedSubject() = relatedSubjectId != 0

    override fun getTypeNameRes() = R.string.event

    override fun getDisplayedTitle() = title

    override fun getRelatedSubject(context: Context) = if (hasRelatedSubject()) {
        Subject.create(context, relatedSubjectId)
    } else {
        null
    }

    override fun getDateTime() = startDateTime

    override fun isInPast() = startDateTime.isBefore(LocalDateTime.now())

    override fun occursOnDate(date: LocalDate) = startDateTime.toLocalDate() == date

    override fun getHomeItemProperties(activity: Activity) = HomeEventProperties(activity, this)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(title)
        dest?.writeString(notes)
        dest?.writeSerializable(startDateTime)
        dest?.writeSerializable(endDateTime)
        dest?.writeString(location)
        dest?.writeInt(relatedSubjectId)
    }

    class HomeEventProperties(context: Context, event: Event) : HomeItemProperties {

        /**
         * The related subject for this event. If there is none, then this will be null.
         */
        private var mSubject: Subject? = null

        init {
            if (event.hasRelatedSubject()) {
                mSubject = Subject.create(context, event.relatedSubjectId)
            }
        }

        override val title = event.title

        override val subtitle = mSubject?.name

        override val time = with(event) {
            "${startDateTime.toLocalTime()}\n${endDateTime.toLocalTime()}"
        }

        override val extraText = null

        override val color = if (mSubject == null) DEFAULT_COLOR else Color(mSubject!!.colorId)

    }

}
