package co.timetableapp.ui.classes

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.Toolbar
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ClassDetailHandler
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.model.Class
import co.timetableapp.model.ClassTime
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.UiUtils

/**
 * Shows the details of a class.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassEditActivity
 * @see ItemDetailActivity
 */
class ClassDetailActivity : ItemDetailActivity<Class>() {

    override fun initializeDataHandler() = ClassHandler(this)

    override fun getLayoutResource() = R.layout.activity_class_detail

    override fun onNullExtras() {
        val intent = Intent(this, ClassEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_ITEM_EDIT)
    }

    override fun setupLayout() {
        val subject = Subject.create(this, mItem!!.subjectId)!!
        setupToolbar(subject)

        val locationBuilder = StringBuilder()
        val teacherBuilder = StringBuilder()
        val allClassTimes = ArrayList<ClassTime>()

        // Add locations, teachers, and times to StringBuilders or ArrayLists, with no duplicates
        ClassDetailHandler.getClassDetailsForClass(this, mItem!!.id).forEach { classDetail ->
            classDetail.formatLocationName()?.let {
                if (!locationBuilder.contains(it)) {
                    locationBuilder.append(it).append("\n")
                }
            }

            if (classDetail.hasTeacher()) {
                if (!teacherBuilder.contains(classDetail.teacher)) {
                    teacherBuilder.append(classDetail.teacher).append("\n")
                }
            }

            allClassTimes.addAll(ClassTimeHandler.getClassTimesForDetail(this, classDetail.id))
        }

        val locationText = findViewById(R.id.textView_location) as TextView
        locationText.text = locationBuilder.toString().removeSuffix("\n")

        val teacherText = findViewById(R.id.textView_teacher) as TextView
        teacherText.text = teacherBuilder.toString().removeSuffix("\n")

        val classTimesText = findViewById(R.id.textView_times) as TextView
        classTimesText.text = produceClassTimesText(allClassTimes)
    }

    private fun setupToolbar(subject: Subject) {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        supportActionBar!!.title = mItem!!.makeName(subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    /**
     * @return the list of class times as a formatted string
     */
    private fun produceClassTimesText(classTimes: ArrayList<ClassTime>): String {
        classTimes.sort()
        val stringBuilder = StringBuilder()

        // Add all time texts - not expecting duplicate values for times
        classTimes.forEach {
            val dayString = it.day.toString()
            val formattedDayString =
                    dayString.substring(0, 1).toUpperCase() + dayString.substring(1).toLowerCase()
            stringBuilder.append(formattedDayString)

            val weekText = it.getWeekText(this)
            if (weekText.isNotEmpty()) {
                stringBuilder.append(" ")
                        .append(weekText)
            }

            stringBuilder.append(", ")
                    .append(it.startTime.toString())
                    .append(" - ")
                    .append(it.endTime.toString())
                    .append("\n")
        }

        return stringBuilder.toString().removeSuffix("\n")
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, ClassEditActivity::class.java)
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        startActivityForResult(intent, REQUEST_CODE_ITEM_EDIT)
    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        finish()
    }

}