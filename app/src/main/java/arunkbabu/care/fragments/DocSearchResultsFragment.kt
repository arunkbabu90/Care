package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Constants
import arunkbabu.care.Doctor
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.activities.PatientActivity
import arunkbabu.care.activities.ViewDoctorProfileActivity
import arunkbabu.care.adapters.DoctorSearchResultsAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.fragment_doc_search_results.*

/**
 * A simple [Fragment] subclass.
 */
class DocSearchResultsFragment(private val specialityId: Int) : Fragment() {
    private lateinit var mAdapter: DoctorSearchResultsAdapter
    private lateinit var mSearchQuery: Query
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore

    companion object {
        const val USER_ID_EXTRAS_KEY = "key_intent_extras_document_id"
        const val DOCTOR_NAME_EXTRAS_KEY = "key_intent_extras_doctor_name"
        const val DOCTOR_DP_EXTRAS_KEY = "key_intent_extras_doctor_profile_picture"

        var docSearchResultsFragmentActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doc_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        docSearchResultsFragmentActive = true

        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseFirestore.getInstance()

        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            val pagingConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(10)
                .build()

            mSearchQuery = mDb.collection(Constants.COLLECTION_DOCTORS_LIST)
                .whereEqualTo(Constants.FIELD_DOCTOR_SPECIALITY, Utils.toSpecialityName(specialityId))
                .whereEqualTo(Constants.FIELD_ACCOUNT_VERIFIED, true)
                .orderBy(Constants.FIELD_FULL_NAME, Query.Direction.ASCENDING)

            val options = FirestorePagingOptions.Builder<Doctor>()
                .setLifecycleOwner(this)
                .setQuery(mSearchQuery, pagingConfig) { snapshot ->
                    val d: Doctor? = snapshot.toObject(Doctor::class.java)
                    if (d != null) {
                        d.documentId = snapshot.id
                    }
                    d ?: Doctor()
                }
                .build()

            mAdapter = DoctorSearchResultsAdapter(options, context = requireContext(), tv_docSearchResults_error,
                swipeRefreshLayout_docSearchResults, rv_docSearchResults,
                { doctor -> onItemClick(doctor) }, { doctor -> onSelectButtonClick(doctor) } )

            rv_docSearchResults.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_docSearchResults.adapter = mAdapter

            swipeRefreshLayout_docSearchResults.setOnRefreshListener { mAdapter.refresh() }
        }
    }

    /**
     * Called when the Doctor Search Item is Clicked
     * @param doctor The selected Doctor
     */
    private fun onItemClick(doctor: Doctor) {
        val viewProfileIntent = Intent(context, ViewDoctorProfileActivity::class.java)
        viewProfileIntent.putExtra(USER_ID_EXTRAS_KEY, doctor.documentId)
        viewProfileIntent.putExtra(DOCTOR_NAME_EXTRAS_KEY, doctor.full_name)
        viewProfileIntent.putExtra(DOCTOR_DP_EXTRAS_KEY, doctor.profilePicture)
        startActivity(viewProfileIntent)
    }

    /**
     * Called when the green Select button on the Doctor Search Item is Clicked
     * @param doctor The selected Doctor
     */
    private fun onSelectButtonClick(doctor: Doctor) {
        val preferredDoc = hashMapOf(Constants.FIELD_PREFERRED_DOCTOR to doctor.documentId)

        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                .set(preferredDoc, SetOptions.merge())
                .addOnSuccessListener {
                    PatientActivity.sReportingDoctorId = doctor.documentId
                    (activity as PatientActivity).fetchDoctorDetails()

                    Toast.makeText(context, getString(R.string.doctor_selected, doctor.full_name), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { Toast.makeText(context, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(context, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        docSearchResultsFragmentActive = false
    }
}