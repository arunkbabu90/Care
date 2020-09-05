package arunkbabu.care.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arunkbabu.care.Doctor
import arunkbabu.care.R
import arunkbabu.care.inflate
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_doctor_search.view.*

class DoctorSearchResultsAdapter(options: FirestorePagingOptions<Doctor>,
                                 private val errTextView: TextView,
                                 private val swipeRefreshLayout: SwipeRefreshLayout)
    : FirestorePagingAdapter<Doctor, DoctorSearchResultsAdapter.SearchResultsViewHolder>(options) {
    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        mContext = parent.context
        val v = parent.inflate(R.layout.item_doctor_search)
        return SearchResultsViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int, model: Doctor) {
        holder.bind(model)
    }

    override fun onError(e: Exception) {
        super.onError(e)
        errTextView.visibility = View.VISIBLE
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        when (state) {
            LoadingState.LOADING_INITIAL -> {
                errTextView.visibility = View.GONE
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
                swipeRefreshLayout.isRefreshing = false
            }

            LoadingState.FINISHED -> {
                swipeRefreshLayout.isRefreshing = false
            }

            else -> super.onLoadingStateChanged(state)
        }
    }

    class SearchResultsViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private lateinit var mTarget: Target

        fun bind(d: Doctor) {
            if (d.profilePicture.isNotBlank()) {
                loadImageToView(Uri.parse(d.profilePicture))
            }
            itemView.tv_docSearchItem_name.text = context.getString(R.string.doc_name, d.full_name)
            itemView.tv_docSearchItem_speciality_qualification.text = context.getString(R.string.doc_speciality_qualification, d.speciality, d.qualification)
            itemView.tv_docSearchItem_hospital.text = d.hospital
        }

        /**
         * Loads an image from the specified URI to the ImageView
         * @param imageUri The URI of the image to be loaded
         */
        private fun loadImageToView(imageUri: Uri) {
            mTarget = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    itemView.iv_docSearchItem_dp.setImageBitmap(bitmap)
                }
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) { }
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
            }
            Picasso.get().load(imageUri).resize(240, 0).into(mTarget)
        }
    }
}