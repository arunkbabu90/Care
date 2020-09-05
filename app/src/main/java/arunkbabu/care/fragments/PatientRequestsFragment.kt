package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Constants
import arunkbabu.care.Patient
import arunkbabu.care.R
import arunkbabu.care.activities.DoctorActivity
import arunkbabu.care.activities.ViewPatientReportActivity
import arunkbabu.care.adapters.RequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_patient_requests.*

class PatientRequestsFragment : Fragment(), RequestListAdapter.ItemClickListener {
    private lateinit var mAdapter: RequestListAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mGetRequestsQuery: Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDb = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        // Get Request details from database
        val user = mAuth.currentUser
        if (user != null) {
            val requestsCollectionPath = Constants.COLLECTION_USERS + "/" + user.uid + "/" + Constants.COLLECTION_PATIENT_REQUEST
            mGetRequestsQuery = mDb.collection(requestsCollectionPath)
                .whereEqualTo(Constants.FIELD_IS_A_VALID_REQUEST, true)
                .orderBy(Constants.FIELD_REQUEST_TIMESTAMP, Query.Direction.ASCENDING)

            val options = FirestoreRecyclerOptions.Builder<Patient>()
                .setLifecycleOwner(this)
                .setQuery(mGetRequestsQuery, Patient::class.java)
                .build()

            mAdapter = RequestListAdapter(options, tv_no_requests, rv_request_view)
            mAdapter.setClickListener(this)
            rv_request_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_request_view.adapter = mAdapter
//            runLayoutAnimation(rv_request_view)
        }
    }

    override fun onItemClick(v: View, patient: Patient, position: Int) {
        when (v.id) {
            R.id.btn_accept -> onAcceptButtonClick(patient, position)
        }
    }

    /**
     * Starts the layout animation
     */
    private fun runLayoutAnimation(recyclerView: RecyclerView) {
        val controller: LayoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_doctors_report)
        recyclerView.layoutAnimation = controller
        recyclerView.scheduleLayoutAnimation()
    }

    /**
     * Accept Button click is handled here
     * @param patient Patient object which contains the patient request details
     * @param position The position of the view clicked.
     */
    private fun onAcceptButtonClick(patient: Patient, position: Int) {
        // Get the basic patient request details
        val docName: String = (activity as DoctorActivity).mDoctorFullName
        val patientId: String = patient.patientId
        val reportId: String = patient.reportId
        val requestId: String = mAdapter.snapshots.getSnapshot(position).id
        val reportType: Int = patient.reportType

        val docIntent = Intent(context, ViewPatientReportActivity::class.java)
        docIntent.putExtra(Constants.PATIENT_ID_KEY, patientId)
        docIntent.putExtra(Constants.PATIENT_REPORT_ID_KEY, reportId)
        docIntent.putExtra(Constants.PATIENT_REPORT_TYPE_KEY, reportType)
        docIntent.putExtra(Constants.PATIENT_REQUEST_ID_KEY, requestId)
        docIntent.putExtra(Constants.DOCTOR_NAME_ID_KEY, docName)
        startActivity(docIntent)
    }

    override fun onStart() {
        super.onStart()
        mGetRequestsQuery.addSnapshotListener { _, _ -> mAdapter.notifyDataSetChanged() }
    }
}