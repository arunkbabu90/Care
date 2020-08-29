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
import arunkbabu.care.Patient
import arunkbabu.care.R
import arunkbabu.care.activities.DoctorActivity
import arunkbabu.care.activities.LoginActivity
import arunkbabu.care.adapters.RequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_patient_requests.*

class PatientRequestsFragment : Fragment(), RequestListAdapter.ItemClickListener, FirebaseAuth.AuthStateListener {
    private lateinit var mAdapter: RequestListAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mGetRequestsQuery: Query
    private var mRequestsCollectionPath: String = ""
    private var mIsLaunched = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDb = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mAuth.addAuthStateListener(this)

        // Get Request details from database
        val user = mAuth.currentUser
        if (user != null) {
            mRequestsCollectionPath = Constants.COLLECTION_USERS + "/" + user.uid + "/" + Constants.COLLECTION_PATIENT_REQUEST
            mGetRequestsQuery = mDb.collection(mRequestsCollectionPath)
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
        }
    }


    override fun onItemClick(v: View, patient: Patient, position: Int) {
        when (v.id) {
            R.id.btn_accept -> onAcceptButtonClick(patient, position)
        }
    }

    /**
     * Accept Button click is handled here
     * @param patient Patient object which contains the patient request details
     * @param position The position of the view clicked.
     */
    private fun onAcceptButtonClick(patient: Patient, position: Int) {
        // Get the basic patient request details
        val patientId = patient.patientId
        val reportId = patient.reportId
        val requestId = mAdapter.snapshots.getSnapshot(position).id
        val reportType = patient.reportType
        val docIntent = Intent(context, DoctorActivity::class.java)
        docIntent.putExtra(Constants.PATIENT_ID_KEY, patientId)
        docIntent.putExtra(Constants.PATIENT_REPORT_ID_KEY, reportId)
        docIntent.putExtra(Constants.PATIENT_REPORT_TYPE_KEY, reportType)
        docIntent.putExtra(Constants.PATIENT_REQUEST_ID_KEY, requestId)
        startActivity(docIntent)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // User is either signed out or the login credentials no longer exists. So launch the login
        // activity again for the user to sign-in
        if (firebaseAuth.currentUser == null && !mIsLaunched) {
            mIsLaunched = true
            startActivity(Intent(context, LoginActivity::class.java))
            Toast.makeText(context, getString(R.string.signed_out), Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mGetRequestsQuery.addSnapshotListener { _, _ -> mAdapter.notifyDataSetChanged() }
    }
}