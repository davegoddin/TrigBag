package net.davegoddin.trigbag

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import net.davegoddin.trigbag.adapter.LogItemAdapter
import net.davegoddin.trigbag.adapter.TrigListItemAdapter
import net.davegoddin.trigbag.data.AppDatabase
import net.davegoddin.trigbag.model.TrigPointDisplay
import net.davegoddin.trigbag.model.Visit
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var sortOptions : Array<String>

    private lateinit var spnSort: Spinner
    private lateinit var visitedPoints : MutableList<TrigPointDisplay>
    private lateinit var adapter: LogItemAdapter
    private lateinit var txtPointCount: TextView
    private lateinit var txtVisitCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //ensure navbar visible
        val navBar : BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar.isVisible = true

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialise variables
        sortOptions = resources.getStringArray(R.array.log_sort_params)
        txtPointCount = view.findViewById(R.id.txt_log_totalPoints)
        txtVisitCount = view.findViewById(R.id.txt_log_totalVisits)
        spnSort = view.findViewById(R.id.spn_log_sort)

        // set up array adapter to populate with options
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSort.setAdapter(arrayAdapter)

        // get visited points and total visits from db
        val db = AppDatabase.getInstance(requireContext())
        visitedPoints = runBlocking { db.trigPointDao().getVisited() }.toMutableList()
        val totalVisits = runBlocking {db.visitDao().getTotalVisits()}

        // update UI
        txtPointCount.text = visitedPoints.count().toString()
        txtVisitCount.text = totalVisits.toString()

        // item selected listener to update recyclerview with sort options
        spnSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // sort points, clear existing set (must maintain consistent reference), add new sorted list and notify recyclerview
                val sortedPoints = sortLog(visitedPoints)
                visitedPoints.clear()
                visitedPoints.addAll(sortedPoints)
                adapter.notifyDataSetChanged()

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        // initialise recyclerview
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_log)
        adapter = LogItemAdapter(this, visitedPoints)
        recyclerView.adapter = adapter

    }

    // sorts list of trigs
    private fun sortLog(list: List<TrigPointDisplay>) : List<TrigPointDisplay>
    {
        // switch for each array item
        when(spnSort.selectedItem)
        {
            sortOptions[0] -> return list.sortedWith(DateComparator().reversed())
            sortOptions[1] -> return list.sortedWith(RatingComparator().reversed())
        }
        return list
    }

    // custom comparator based on date of last visit
    inner class DateComparator: Comparator<TrigPointDisplay>
    {
        override fun compare(p0: TrigPointDisplay?, p1: TrigPointDisplay?): Int {
            if (p0 == null || p1 == null) return 0

            //sort visit lists by descending date and compare
            val p0Visits = p0.visits.sortedByDescending{visit -> visit.dateTime}
            val p1Visits = p1.visits.sortedByDescending { visit -> visit.dateTime }

            return p0Visits[0].dateTime!!.compareTo(p1Visits[0].dateTime!!)
        }
    }

    // comparator based on average rating
    inner class RatingComparator: Comparator<TrigPointDisplay>
    {
        override fun compare(p0: TrigPointDisplay?, p1: TrigPointDisplay?): Int {
            if(p0 == null || p1 == null) return 0
            //compare based on average rating
            return getAverageRating(p0.visits).compareTo(getAverageRating(p1.visits))
        }

        private fun getAverageRating(visits: List<Visit>) : Float
        {
            //get mean rating of all visits on point - should always have visits
            var avgRating = 0f
            var ratingCount = 0
            visits.forEach {
                if (it.rating != null && it.rating != 0f)
                {
                    ratingCount++
                    avgRating += it.rating
                }
            }
            avgRating /= ratingCount
            return avgRating
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TrigsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}