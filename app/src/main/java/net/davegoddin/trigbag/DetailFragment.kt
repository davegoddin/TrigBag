package net.davegoddin.trigbag

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import net.davegoddin.trigbag.adapter.VisitListItemAdapter
import net.davegoddin.trigbag.data.AppDatabase
import net.davegoddin.trigbag.model.TrigPointDisplay
import net.davegoddin.trigbag.model.Visit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "trigPointDisplay"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var trigPointDisplay : TrigPointDisplay

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var btnDate: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnAddVisit: ImageButton
    private lateinit var txtName: TextView
    private lateinit var txtLatLon: TextView
    private lateinit var txtCondition: TextView
    private lateinit var txtCountry: TextView
    private lateinit var txtType: TextView
    private lateinit var txtComment : TextView
    private lateinit var ratRating: RatingBar
    private lateinit var conForm: ConstraintLayout

    private lateinit var recAdapter: VisitListItemAdapter

    private var newVisitDate = LocalDate.now()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val trigPointDisplayJSON = it.getString(ARG_PARAM1)
            trigPointDisplay = Gson().fromJson(trigPointDisplayJSON, TrigPointDisplay::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val navBar : BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        navBar.isVisible = false

        (requireActivity() as AppCompatActivity).supportActionBar?.title = trigPointDisplay.trigPoint.name

        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatePicker()

        // initialise UI components
        btnDate = view.findViewById(R.id.btn_detail_datepicker)
        btnAddVisit = view.findViewById(R.id.btn_detail_addVisit)
        btnSubmit = view.findViewById(R.id.btn_detail_submit)
        txtName = view.findViewById(R.id.txt_detail_name)
        txtCondition = view.findViewById(R.id.txt_detail_condition)
        txtCountry = view.findViewById(R.id.txt_detail_country)
        txtLatLon = view.findViewById(R.id.txt_detail_latlon)
        txtType = view.findViewById(R.id.txt_detail_type)
        txtComment = view.findViewById(R.id.txt_detail_comment)
        ratRating = view.findViewById(R.id.rat_detail_rating)
        conForm = view.findViewById(R.id.con_detail_visitForm)


        // visit recyclerview
        val recVisits : RecyclerView = view.findViewById(R.id.recycler_detail)
        recAdapter = VisitListItemAdapter(this, trigPointDisplay.visits)
        recVisits.adapter = recAdapter

        // button click listeners
        btnSubmit.setOnClickListener(addVisit())

        btnDate.setOnClickListener{
            openDatePicker(it)
        }
        btnDate.text = makeDateString(LocalDate.now().dayOfMonth, LocalDate.now().monthValue, LocalDate.now().year)

        btnAddVisit.setOnClickListener {
            if (!conForm.isVisible)
            {
                showForm()
            }
            else
            {
                hideForm()
            }
        }

        // trigpoint display attributes
        txtName.text = trigPointDisplay.trigPoint.name
        txtCondition.text = trigPointDisplay.trigPoint.condition
        txtCountry.text = trigPointDisplay.trigPoint.country
        txtLatLon.text = "${trigPointDisplay.trigPoint.latitude}, ${trigPointDisplay.trigPoint.longitude}"
        txtType.text = trigPointDisplay.trigPoint.type


    }

    private fun initDatePicker()
    {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                //validate date is not in the future
                val enteredDate = LocalDate.of(year, month+1, day)
                if (enteredDate <= LocalDate.now())
                {
                    val date : String = makeDateString(day, month+1, year)
                    newVisitDate = enteredDate
                    btnDate.text = date
                }
                else
                {
                    Snackbar.make(requireView(), "Date cannot be in the future", Snackbar.LENGTH_SHORT).show()
                }
            }
        datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth )
    }

    fun openDatePicker(View: View)
    {
        datePickerDialog.show()
    }

    private fun makeDateString(day: Int, month: Int, year: Int) : String
    {
        val date : LocalDate = LocalDate.of(year, month, day)
        return date.format(DateTimeFormatter.ofPattern("dd LLL yyyy"))
    }

    private fun addVisit() = View.OnClickListener()
    {
        // get details from form (null permissible for comment and rating)
        val comment = txtComment.text.toString()
        val rating = ratRating.rating
        val date = newVisitDate.toEpochDay()

        val visit = Visit(date, trigPointDisplay.trigPoint.id, rating, comment)

        // add to current item and notify recyclerview
        trigPointDisplay.visits.add(0, visit)
        recAdapter.notifyItemInserted(0)

        // add to db
        val db = AppDatabase.getInstance(requireContext())
        runBlocking {
            db.visitDao().insert(visit)
        }

        // clear fields
        hideForm()

    }

    private fun hideForm() {
        // clear form fileds
        txtComment.text = null
        ratRating.rating = 0f
        newVisitDate = LocalDate.now()
        btnDate.text = makeDateString(LocalDate.now().dayOfMonth, LocalDate.now().monthValue, LocalDate.now().year)
        // hide
        conForm.visibility = View.GONE
        // change button
        btnAddVisit.setImageResource(R.drawable.ic_baseline_add_circle_24)
        // hide keyboard
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showForm() {
        conForm.visibility = View.VISIBLE
        btnAddVisit.setImageResource(R.drawable.ic_baseline_cancel_24)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

