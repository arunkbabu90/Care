package arunkbabu.care.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.R
import arunkbabu.care.Speciality
import arunkbabu.care.inflate
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_doctor_search_category.view.*

class DoctorCategoryAdapter(private val specialities: ArrayList<Speciality>) : RecyclerView.Adapter<DoctorCategoryAdapter.DoctorCategoryViewHolder>() {
    var mListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorCategoryViewHolder {
        val view = parent.inflate(R.layout.item_doctor_search_category, false)
        return DoctorCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorCategoryViewHolder, position: Int) {
        holder.bind(specialities[position])
    }

    override fun getItemCount(): Int = specialities.size

    inner class DoctorCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var mSpeciality: Speciality? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(speciality: Speciality) {
            mSpeciality = speciality
            (itemView as MaterialCardView).setCardBackgroundColor(ContextCompat.getColor(itemView.context, speciality.backgroundColor))
            itemView.category_image_view.setImageFromResource(speciality.imageResource)
            itemView.category_title_text_view.text = speciality.title
            itemView.category_description_text_view.text = speciality.description
            itemView.category_image_view.setIconTint(speciality.backgroundColor)
        }

        /**
         * Item Click Listener
         */
        override fun onClick(v: View?) {
            if (v != null && mListener != null) {
                mListener?.onItemClick(v, adapterPosition)
            }
        }
    }

    /**
     * Register a callback to be invoked when an item is clicked
     */
    fun setOnClickListener(listener: ItemClickListener) {
        mListener = listener
    }

    /**
     * Interface definition for a callback to be invoked when an item is clicked
     */
    interface ItemClickListener {
        fun onItemClick(v: View, position: Int)
    }
}