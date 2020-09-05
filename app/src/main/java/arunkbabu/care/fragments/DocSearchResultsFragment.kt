package arunkbabu.care.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Constants
import arunkbabu.care.Doctor
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.adapters.DoctorSearchResultsAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_doc_search_results.*

/**
 * A simple [Fragment] subclass.
 */
class DocSearchResultsFragment(private val specialityId: Int) : Fragment() {
    private lateinit var mAdapter: DoctorSearchResultsAdapter
    private lateinit var mSearchQuery: Query
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doc_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                .setQuery(mSearchQuery, pagingConfig, Doctor::class.java)
                .build()

            mAdapter = DoctorSearchResultsAdapter(options, tv_docSearchResults_error, swipeRefreshLayout_docSearchResults)
            rv_docSearchResults.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_docSearchResults.adapter = mAdapter

            swipeRefreshLayout_docSearchResults.setOnRefreshListener { mAdapter.refresh() }
        }
    }
}