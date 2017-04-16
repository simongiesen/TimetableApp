package co.timetableapp.ui.timetables

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.PortingFragment
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.model.Timetable
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.UiUtils
import java.util.*

/**
 * A fragment for displaying a list of timetables to the user.
 *
 * Note that unlike other list items, there cannot be a placeholder background since there is always
 * at least one existing timetable in the app's database.
 *
 * @see Timetable
 * @see TimetablesActivity
 * @see TimetableEditActivity
 */
class TimetablesFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_TIMETABLE_EDIT = 1
    }

    private var mTimetables: ArrayList<Timetable>? = null

    private var mAdapter: TimetablesAdapter? = null
    private var mDataHandler: TimetableHandler? = null

    private var mRootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater!!.inflate(R.layout.fragment_content_list, container, false)

        mDataHandler = TimetableHandler(activity)
        setupLayout()

        return mRootView
    }

    private fun setupLayout() {
        setupList()

        activity.findViewById(R.id.fab).setOnClickListener {
            val intent = Intent(activity, TimetableEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT)
        }
    }

    private fun setupList() {
        mTimetables = mDataHandler!!.getAllItems()
        sortList()

        mAdapter = TimetablesAdapter(activity, mTimetables, activity.findViewById(R.id.coordinatorLayout))
        mAdapter!!.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, TimetableEditActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mTimetables!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT, bundle)
        }

        val recyclerView = mRootView!!.findViewById(R.id.recyclerView) as RecyclerView
        with(recyclerView) {
            val linearLayoutManager = LinearLayoutManager(activity)
            layoutManager = linearLayoutManager
            val dividerDecoration = DividerItemDecoration(activity, linearLayoutManager.orientation)
            addItemDecoration(dividerDecoration)

            setHasFixedSize(true)
            adapter = mAdapter
        }
    }

    private fun sortList() = mTimetables!!.sort()

    private fun refreshList() {
        mTimetables!!.clear()
        mTimetables!!.addAll(mDataHandler!!.getAllItems())
        sortList()
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_timetables, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handles import/export options.
        val portType = when (item!!.itemId) {
            R.id.action_export -> PortingFragment.TYPE_EXPORT
            R.id.action_import -> PortingFragment.TYPE_IMPORT
            else -> throw IllegalArgumentException("menu option not supported")
        }
        startPortingFragment(portType)

        return super.onOptionsItemSelected(item)
    }

    private fun startPortingFragment(portType: Int) {
        val fragmentArgs = Bundle()
        fragmentArgs.putInt(PortingFragment.ARGUMENT_PORT_TYPE, portType)

        val portFragment = PortingFragment()
        portFragment.onPortingCompleteListener = object : PortingFragment.OnPortingCompleteListener {
            override fun onPortingComplete(portingType: Int, successful: Boolean) {
                refreshList()

                val currentTimetable = (activity.application as TimetableApplication).currentTimetable!!
                val message = getString(R.string.message_set_current_timetable,
                        currentTimetable.displayedName)

                Snackbar.make(activity.findViewById(R.id.coordinatorLayout),
                        message,
                        Snackbar.LENGTH_LONG).show()
            }
        }
        portFragment.arguments = fragmentArgs

        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.add(portFragment, null)
        transaction.commit()
    }

}
