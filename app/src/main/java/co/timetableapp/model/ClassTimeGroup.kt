package co.timetableapp.model

import org.threeten.bp.LocalTime
import java.util.*

/**
 * A data structure for grouping [ClassTime] objects with the same start and end dates as each
 * other.
 *
 * @property startTime the start time all `ClassTime`s in this group will have
 * @property endTime the end time all `ClassTime`s in this group will have
 *
 * @see ClassTime
 */
class ClassTimeGroup(val startTime: LocalTime, val endTime: LocalTime) : Iterable<ClassTime> {

    val classTimes = ArrayList<ClassTime>()

    /**
     * Adds a [ClassTime] to this group.
     *
     * @param classTime The [ClassTime] to be added to this group. It must have the same start and
     *          end times as the ones specified in this class.
     * @see startTime
     * @see endTime
     */
    fun addClassTime(classTime: ClassTime) {
        if (!canAdd(classTime)) {
            throw IllegalArgumentException("invalid class time - the start and end times must" +
                    "match the ones specified from this object's constructor")
        }
        classTimes.add(classTime)
    }

    /**
     * @return if the [classTime] can be added to the group (if it has the same start and end times)
     */
    fun canAdd(classTime: ClassTime) =
            classTime.startTime == startTime && classTime.endTime == endTime


    override fun iterator() = ClassTimeIterator()

    inner class ClassTimeIterator : Iterator<ClassTime> {

        private var currentIndex = 0

        override fun hasNext() = currentIndex < classTimes.size

        override fun next() = classTimes[currentIndex++]

    }

}