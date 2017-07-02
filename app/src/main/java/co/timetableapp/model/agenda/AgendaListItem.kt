package co.timetableapp.model.agenda

import android.os.Parcelable
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents any item which can be displayed on a list of Agenda items, including headers.
 */
interface AgendaListItem : Comparable<AgendaListItem>, Parcelable {

    fun isHeader(): Boolean

    /**
     * @return  the date and time of the item. If there is no time, then [LocalTime.MIDNIGHT] should
     *          be used as the time. This is used for sorting Agenda list items.
     */
    fun getDateTime(): LocalDateTime

    override fun compareTo(other: AgendaListItem): Int{
        val dateTimeComparison = getDateTime().compareTo(other.getDateTime())
        if (dateTimeComparison != 0) {
            return dateTimeComparison
        }

        val itemTypeComparison = compareItemTypes(other)
        if (itemTypeComparison != 0) {
            return itemTypeComparison
        }

        // Item type comparison is 0 so both must be headers
        return (this as AgendaHeader).compareHeaders(other as AgendaHeader)
    }

    /**
     * @return  an integer between -1 and 1 inclusive based on the comparing whether this item and
     *          the [other] item are headers or not.
     */
    private fun compareItemTypes(other: AgendaListItem): Int {
        return if (isHeader()) {
            if (other.isHeader()) {
                0 // equal - both are headers
            } else {
                -1 // this header comes before the other item
            }
        } else {
            1
        }
    }

}
