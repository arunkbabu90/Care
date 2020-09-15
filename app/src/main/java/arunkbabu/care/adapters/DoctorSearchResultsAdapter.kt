package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arunkbabu.care.Doctor
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.inflate
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import kotlinx.android.synthetic.main.item_doctor_search.view.*

class DoctorSearchResultsAdapter(options: FirestorePagingOptions<Doctor>,
                                 private val context: Context,
                                 private val errTextView: TextView,
                                 private val swipeRefreshLayout: SwipeRefreshLayout,
                                 private val recyclerView: RecyclerView,
                                 private val itemClickListener: (Doctor) -> Unit,
                                 private val selectBtnClickListener: (Doctor) -> Unit)
    : FirestorePagingAdapter<Doctor, DoctorSearchResultsAdapter.SearchResultsViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val v = parent.inflate(R.layout.item_doctor_search)
        return SearchResultsViewHolder(v, parent.context)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int, model: Doctor) {
        holder.bind(model, itemClickListener, selectBtnClickListener)
    }

    override fun onError(e: Exception) {
        super.onError(e)
        errTextView.visibility = View.VISIBLE
        errTextView.text = context.getString(R.string.err_load_failed_default)
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        when (state) {
            LoadingState.LOADING_INITIAL -> {
                errTextView.visibility = View.GONE
                recyclerView.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = true
            }

            LoadingState.LOADING_MORE -> {
                swipeRefreshLayout.isRefreshing = true
            }

            LoadingState.LOADED -> {
                swipeRefreshLayout.isRefreshing = false
            }

            LoadingState.ERROR -> {
                errTextView.visibility = View.VISIBLE
                errTextView.text = context.getString(R.string.err_load_failed_default)
                swipeRefreshLayout.isRefreshing = false
            }

            LoadingState.FINISHED -> {
                swipeRefreshLayout.isRefreshing = false
                if (itemCount <= 0) {
                    errTextView.visibility = View.VISIBLE
                    errTextView.text = context.getString(R.string.err_no_doctors_found)
                    recyclerView.visibility = View.GONE
                } else {
                    errTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                Utils.runStackedRevealAnimation(context, recyclerView, true)
            }

            else -> super.onLoadingStateChanged(state)
        }
    }

    class SearchResultsViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        fun bind(d: Doctor, itemClickListener: (Doctor) -> Unit, selectBtnClickListener: (Doctor) -> Unit) {
            if (d.profilePicture.isNotBlank()) {
                Utils.loadDpToView(context, d.profilePicture, itemView.iv_docSearchItem_dp)
            }
            itemView.tv_docSearchItem_name.text = context.getString(R.string.doc_name, d.full_name)
            itemView.tv_docSearchItem_speciality_qualification.text = context.getString(R.string.doc_speciality_qualification,
                d.speciality,
                d.qualification)
            itemView.tv_docSearchItem_hospital.text = d.hospital

            itemView.setOnClickListener { itemClickListener(d) }
            itemView.btn_docSearchItem_select.setOnClickListener { selectBtnClickListener(d) }
        }
    }
}