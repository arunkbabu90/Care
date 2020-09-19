package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arunkbabu.care.R
import arunkbabu.care.Speciality
import arunkbabu.care.fragments.DocSearchResultsFragment
import arunkbabu.care.fragments.DoctorSearchCategoryFragment

class SearchDoctorActivity : AppCompatActivity() {
    companion object {
        var searchDoctorActivityActive = false
        var reportingDoctorId = ""
            set(value) {
                field = value
                if (OtherUntowardActivity.isOtherUntowardActivityActive) {
                    OtherUntowardActivity.sReportingDoctorId = value
                    if (value.isNotBlank())
                        OtherUntowardActivity.sIsNewDoctorSelected = true
                }
            }

        var instance: SearchDoctorActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_doctor)
        instance = this

        supportFragmentManager.beginTransaction()
            .add(R.id.search_doctor_fragment_container, DoctorSearchCategoryFragment())
            .commit()

        searchDoctorActivityActive = true
    }

    /**
     * Called when a Speciality-Category in DoctorSearchCategoryFragment is clicked
     * @param speciality The speciality category clicked
     */
    fun onDocCategoryClick(speciality: Speciality) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_doctor_fragment_container, DocSearchResultsFragment(speciality.id))
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchDoctorActivityActive = false
    }
}