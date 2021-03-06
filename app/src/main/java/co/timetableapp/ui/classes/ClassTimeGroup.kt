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

package co.timetableapp.ui.classes

import co.timetableapp.model.ClassTime
import org.threeten.bp.LocalTime

/**
 * A data structure for grouping [ClassTime] objects with the same start and end dates as each
 * other.
 *
 * @property startTime  the start time all `ClassTime`s in this group will have
 * @property endTime    the end time all `ClassTime`s in this group will have
 *
 * @see ClassTime
 */
class ClassTimeGroup(val startTime: LocalTime, val endTime: LocalTime) : Iterable<ClassTime> {

    val classTimes = ArrayList<ClassTime>()

    /**
     * Adds a [ClassTime] to this group.
     *
     * @param classTime the [ClassTime] to be added to this group. It must have the same start and
     *                  end times as those specified in this class.
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

    /**
     * @return a list of time groups, sorted in the correct order
     */
    fun sortedAndGrouped(): ArrayList<ClassTimeGroup> {
        if (classTimes.isEmpty()) {
            return ArrayList()
        }

        classTimes.sortWith(ClassTime.TimeComparator())

        val classTimeGroups = ArrayList<ClassTimeGroup>()
        var currentGroup: ClassTimeGroup? = null

        for (classTime in classTimes) {
            if (currentGroup == null) {
                currentGroup = ClassTimeGroup(classTime.startTime, classTime.endTime)
                currentGroup.addClassTime(classTime)
                continue
            }

            if (currentGroup.canAdd(classTime)) {
                currentGroup.addClassTime(classTime)
            } else {
                classTimeGroups.add(currentGroup) // add what has been produced up to this point
                currentGroup = ClassTimeGroup(classTime.startTime, classTime.endTime)
                currentGroup.addClassTime(classTime)
            }
        }

        currentGroup?.let {
            classTimeGroups.add(it) // add what was made in the last round of iterations
        }

        return classTimeGroups
    }


    override fun iterator() = ClassTimeIterator()

    inner class ClassTimeIterator : Iterator<ClassTime> {

        private var currentIndex = 0

        override fun hasNext() = currentIndex < classTimes.size

        override fun next() = classTimes[currentIndex++]

    }

}
