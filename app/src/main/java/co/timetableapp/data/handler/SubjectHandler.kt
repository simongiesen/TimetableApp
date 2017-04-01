package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassesSchema
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.data.schema.SubjectsSchema
import co.timetableapp.model.Subject

class SubjectHandler(context: Context) : TimetableItemHandler<Subject>(context) {

    override val tableName = SubjectsSchema.TABLE_NAME

    override val itemIdCol = SubjectsSchema._ID

    override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)

    override fun createFromId(id: Int) = Subject.create(context, id)

    override fun propertiesAsContentValues(item: Subject): ContentValues {
        val values = ContentValues()
        with(values) {
            put(SubjectsSchema._ID, item.id)
            put(SubjectsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(SubjectsSchema.COL_NAME, item.name)
            put(SubjectsSchema.COL_ABBREVIATION, item.abbreviation)
            put(SubjectsSchema.COL_COLOR_ID, item.colorId)
        }
        return values
    }

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        val classesQuery = Query.Builder()
                .addFilter(Filters.equal(ClassesSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val classUtils = ClassHandler(context)
        classUtils.getAllItems(classesQuery).forEach {
            classUtils.deleteItemWithReferences(it.id)
        }

        val examsQuery = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val examUtils = ExamHandler(context)
        examUtils.getAllItems(examsQuery).forEach {
            examUtils.deleteItem(it.id)
        }
    }

}
