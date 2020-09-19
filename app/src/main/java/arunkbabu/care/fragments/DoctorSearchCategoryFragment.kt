package arunkbabu.care.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.R
import arunkbabu.care.Speciality
import arunkbabu.care.activities.PatientActivity
import arunkbabu.care.activities.SearchDoctorActivity
import arunkbabu.care.adapters.DoctorCategoryAdapter
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_doctor_search_category.*

/**
 * A simple [Fragment] subclass.
 */
class DoctorSearchCategoryFragment : Fragment(), DoctorCategoryAdapter.ItemClickListener {

    private lateinit var adapter: DoctorCategoryAdapter
    private val specialities: ArrayList<Speciality> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_search_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        specialities.add(Speciality(Speciality.CARDIOLOGIST, R.drawable.ic_heart, getString(R.string.cardiologist), getString(R.string.cardiologist_desc), R.color.colorCatRed))
        specialities.add(Speciality(Speciality.PEDIATRICIAN, R.drawable.ic_pediatrician, getString(R.string.pediatrician), getString(R.string.pediatrician_desc), R.color.colorCatBlue))
        specialities.add(Speciality(Speciality.GENERAL, R.drawable.ic_general_medicine, getString(R.string.general), getString(R.string.general_desc), R.color.colorGreen))
        specialities.add(Speciality(Speciality.ONCOLOGIST, R.drawable.ic_cancer, getString(R.string.oncologist), getString(R.string.oncologist_desc), R.color.colorCatYellow))
        specialities.add(Speciality(Speciality.OTHER, R.drawable.ic_other, getString(R.string.other), getString(R.string.other_desc), R.color.colorCatIndigo))

        adapter = DoctorCategoryAdapter(specialities)
        adapter.setOnClickListener(this)
        rv_doc_category.layoutManager = LinearLayoutManager(context)
        rv_doc_category.adapter = adapter
    }

    override fun onItemClick(v: View, position: Int) {
        if (specialities.isNotEmpty()) {
            if (SearchDoctorActivity.searchDoctorActivityActive) {
                (activity as SearchDoctorActivity).onDocCategoryClick(specialities[position])
            } else {
                (activity as PatientActivity).onDocCategoryClick(specialities[position])
            }
        } else {
            Toast.makeText(context, R.string.err_default, Toast.LENGTH_LONG).show()
        }
    }
}