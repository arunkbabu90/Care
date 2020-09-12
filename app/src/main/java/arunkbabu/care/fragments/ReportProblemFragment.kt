package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import arunkbabu.care.activities.OtherUntowardActivity
import arunkbabu.care.activities.PatientActivity
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_report_problem.*

/**
 * A simple [Fragment] subclass.
 */
class ReportProblemFragment : Fragment(), View.OnClickListener {

    companion object {
        const val REPORTING_DOCTOR_ID_EXTRAS_KEY = "key_report_problem_extras_reporting_doctor_id"
        const val PATIENT_NAME_EXTRAS_KEY = "key_report_problem_extras_patient_name"
        const val PATIENT_SEX_EXTRAS_KEY = "key_report_problem_extras_patient_sex"
        const val PATIENT_DP_EXTRAS_KEY = "key_report_problem_extras_patient_dp"
        const val DOCTOR_DP_EXTRAS_KEY = "key_report_problem_doctor_dp"
        const val DOCTOR_NAME_EXTRAS_KEY = "key_report_problem_doctor_name"

        var reportProblemFragmentActive = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_problem, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_report_problem.isEnabled = false
        reportProblemFragmentActive = true

        if (PatientActivity.isDataLoaded)
            activateViews()
        else
            pb_report_problem_loading.visibility = View.VISIBLE

        btn_report_problem.setOnClickListener(this)
    }

    /**
     * Enable the views in this fragment
     */
    fun activateViews() {
        btn_report_problem.isEnabled = true
        pb_report_problem_loading.visibility = View.GONE
    }

    /**
     * Helper method to hide the loading Circle
     */
    fun hideLoadingCircle() {
        pb_report_problem_loading.visibility = View.GONE
    }

    /**
     * Helper method to show the loading Circle
     */
    fun showLoadingCircle() {
        pb_report_problem_loading.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_report_problem -> {
                val pa = (activity as PatientActivity)
                val reportProblemIntent = Intent(context, OtherUntowardActivity::class.java)
                reportProblemIntent.putExtra(REPORTING_DOCTOR_ID_EXTRAS_KEY, PatientActivity.sReportingDoctorId)
                reportProblemIntent.putExtra(PATIENT_NAME_EXTRAS_KEY, pa.fullName)
                reportProblemIntent.putExtra(PATIENT_SEX_EXTRAS_KEY, pa.sex)
                reportProblemIntent.putExtra(PATIENT_DP_EXTRAS_KEY, pa.patientDpPath)
                reportProblemIntent.putExtra(DOCTOR_DP_EXTRAS_KEY, pa.docDpPath)
                reportProblemIntent.putExtra(DOCTOR_NAME_EXTRAS_KEY, pa.docName)
                startActivity(reportProblemIntent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reportProblemFragmentActive = false
    }
}