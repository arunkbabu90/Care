package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Constants
import arunkbabu.care.DoctorReport
import arunkbabu.care.R
import arunkbabu.care.activities.ViewDoctorReportActivity
import arunkbabu.care.adapters.ReportListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_doctors_report.*

/**
 * A simple [Fragment] subclass.
 */
class DoctorsReportFragment : Fragment(), ReportListAdapter.ItemClickListener {
    companion object {
        const val KEY_EXTRA_REPORT_ID = "report_id_key_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctors_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val docReportPath: String = Constants.COLLECTION_USERS + "/" + user.uid + "/" + Constants.COLLECTION_DOCTOR_REPORT
            val docReportQuery: Query = db.collection(docReportPath)
                .whereEqualTo(Constants.FIELD_IS_A_VALID_DOCTOR_REPORT, true)
                .orderBy(Constants.FIELD_REQUEST_TIMESTAMP, Query.Direction.DESCENDING)

            val options = FirestoreRecyclerOptions.Builder<DoctorReport>()
                .setLifecycleOwner(this)
                .setQuery(docReportQuery, DoctorReport::class.java).build()

            val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = ReportListAdapter(options, rv_doctor_reports, tv_doctor_reports_no_requests)
            rv_doctor_reports.setHasFixedSize(false)
            rv_doctor_reports.layoutManager = lm
            rv_doctor_reports.adapter = adapter
            adapter.setClickListener(this)
        }
    }

    override fun onItemClick(v: View?, report: DoctorReport, position: Int) {
        // On Doctor Report item click
        val viewReportIntent = Intent(context, ViewDoctorReportActivity::class.java)
        viewReportIntent.putExtra(KEY_EXTRA_REPORT_ID, report.reportId)
        startActivity(viewReportIntent)
    }
}