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

package co.timetableapp.licenses

import android.os.Parcel
import android.os.Parcelable

class Library(val name: String, val website: String?, val author: String,
              val license: License) : Comparable<Library>, Parcelable {

    override fun compareTo(other: Library) = name.compareTo(other.name)

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Library> = object : Parcelable.Creator<Library> {
            override fun createFromParcel(source: Parcel): Library = Library(source)
            override fun newArray(size: Int): Array<Library?> = arrayOfNulls(size)
        }
    }

    private constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<License>(License::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(website)
        dest?.writeString(author)
        dest?.writeParcelable(license, 0)
    }
}
