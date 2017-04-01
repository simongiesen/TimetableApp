package co.timetableapp.licenses

import android.content.Context
import android.os.Parcelable
import co.timetableapp.R

interface License : Comparable<License>, Parcelable {

    val name: String

    fun getNotice(context: Context): String {
        return context.resources.getString(R.string.license_apache_2_0)
    }

    override fun compareTo(other: License) = name.compareTo(other.name)

}