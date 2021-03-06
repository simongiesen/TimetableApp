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

package co.timetableapp.ui.base

import android.support.v7.widget.RecyclerView
import android.view.View
import co.timetableapp.data.handler.TimetableItemHandler
import co.timetableapp.model.TimetableItem

/**
 * UI components displaying a list of items should implement this to define behavior specific to the
 * type of item being shown (e.g. assignments are displayed differently to classes).
 *
 * @param T the type of items being displayed
 */
internal interface ItemListImpl<T : TimetableItem> {

    /**
     * @return a new data handler instance relevant to the type of items being displayed
     */
    fun instantiateDataHandler(): TimetableItemHandler<T>

    fun setupLayout()

    /**
     * Populates the items list with data from the database table for the current timetable, before
     * sorting this and displaying it.
     *
     * This includes fetching the items, sorting the list, and setting up the adapter and
     * [RecyclerView].
     *
     * @see fetchItems
     * @see sortList
     * @see setupAdapter
     */
    fun setupList()

    /**
     * Subclasses should use this to specify items to display in the UI.
     *
     * @return a list of items to use when populating the user interface.
     */
    fun fetchItems(): ArrayList<T>

    /**
     * Instantiates, sets up, and returns the relevant RecyclerView adapter.
     * The setup process may involve setting properties, adding listeners, etc.
     */
    fun setupAdapter(): RecyclerView.Adapter<*>

    /**
     * Sorts the list of items being displayed.
     */
    fun sortList()

    /**
     * Updates the list with any modified or removed data. If there is none to display, the
     * placeholder layout is shown instead.
     *
     * @see fetchItems
     * @see sortList
     * @see refreshPlaceholderStatus
     */
    fun updateList()

    /**
     * Displays the placeholder layout if the list of items is empty.
     *
     * @see getPlaceholderView
     */
    fun refreshPlaceholderStatus()

    /**
     * @return the view used as the placeholder layout for the subclass activity.
     *
     * @see refreshPlaceholderStatus()
     */
    fun getPlaceholderView(): View

}
