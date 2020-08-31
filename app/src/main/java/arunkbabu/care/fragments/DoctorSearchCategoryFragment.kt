package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Speciality
import arunkbabu.care.activities.SearchResultsActivity
import arunkbabu.care.adapters.DoctorCategoryAdapter
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_doctor_search_category.*

/**
 * A simple [Fragment] subclass.
 */
class DoctorSearchCategoryFragment : Fragment(), DoctorCategoryAdapter.ItemClickListener {
    private lateinit var mAdapter: DoctorCategoryAdapter
    private val mSpecialities: ArrayList<Speciality> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_search_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSpecialities.add(Speciality(Constants.SPECIALITY_CARDIOLOGIST, R.drawable.ic_heart, getString(R.string.cardiologist), getString(R.string.cardiologist_desc), R.color.colorCatRed))
        mSpecialities.add(Speciality(Constants.SPECIALITY_PEDIATRICIAN, R.drawable.ic_pediatrician, getString(R.string.pediatrician), getString(R.string.pediatrician_desc), R.color.colorCatBlue))
        mSpecialities.add(Speciality(Constants.SPECIALITY_GENERAL, R.drawable.ic_general_medicine, getString(R.string.general), getString(R.string.general_desc), R.color.colorGreen))
        mSpecialities.add(Speciality(Constants.SPECIALITY_ONCOLOGIST, R.drawable.ic_cancer, getString(R.string.oncologist), getString(R.string.oncologist_desc), R.color.colorCatYellow))

        mAdapter = DoctorCategoryAdapter(mSpecialities)
        mAdapter.setOnClickListener(this)
        rv_doc_category.layoutManager = LinearLayoutManager(context)
        rv_doc_category.adapter = mAdapter
    }

    override fun onItemClick(v: View, position: Int) {
        if (mSpecialities.isNotEmpty()) {
            val showResultsIntent = Intent(activity, SearchResultsActivity::class.java)
            showResultsIntent.putExtra(SearchResultsActivity.KEY_EXTRA_SPECIALITY_ID, mSpecialities[position].id)
            startActivity(showResultsIntent)
        } else {
            Toast.makeText(context, R.string.err_default, Toast.LENGTH_LONG).show()
        }
    }
}