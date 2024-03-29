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
import arunkbabu.care.Utils
import arunkbabu.care.activities.ViewDoctorReportActivity
import arunkbabu.care.adapters.ReportListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_doctors_reports.*

class DoctorsReportsFragment : Fragment(), ReportListAdapter.ItemClickListener {
    companion object {
        const val KEY_EXTRA_REPORT_ID = "report_id_key_extra"
        const val KEY_EXTRA_REPORT_TIMESTAMP = "report_timestamp_extra_key"
    }
    private lateinit var adapter: ReportListAdapter
    private lateinit var docReportQuery: Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctors_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val docReportPath =  "${Constants.COLLECTION_USERS}/${user.uid}/${Constants.COLLECTION_DOCTOR_REPORT}"
            docReportQuery = db.collection(docReportPath)
                .whereEqualTo(Constants.FIELD_IS_A_VALID_DOCTOR_REPORT, true)
                .orderBy(Constants.FIELD_REQUEST_TIMESTAMP, Query.Direction.DESCENDING)

            val options = FirestoreRecyclerOptions.Builder<DoctorReport>()
                .setLifecycleOwner(this)
                .setQuery(docReportQuery, DoctorReport::class.java)
                .build()

            adapter = ReportListAdapter(options, rv_doctor_reports, tv_doctor_reports_no_requests)
            adapter.setClickListener(this)
            rv_doctor_reports.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_doctor_reports.adapter = adapter
        }
    }

    override fun onItemClick(v: View?, report: DoctorReport, position: Int) {
        // On Doctor Report item click
        val date: String = Utils.convertDateToString(report.requestTimestamp.toDate(), "dd MMMM yyyy")

        val viewReportIntent = Intent(context, ViewDoctorReportActivity::class.java)
        viewReportIntent.putExtra(KEY_EXTRA_REPORT_ID, report.reportId)
        viewReportIntent.putExtra(KEY_EXTRA_REPORT_TIMESTAMP, date)
        startActivity(viewReportIntent)
    }

    override fun onStart() {
        super.onStart()
        docReportQuery.addSnapshotListener { _, _ -> adapter.notifyDataSetChanged() }
    }
}