package arunkbabu.care.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.R
import arunkbabu.care.inflate
import kotlinx.android.synthetic.main.item_doctor_profile.view.*

class DoctorProfileAdapter(private val data: ArrayList<String>) : RecyclerView.Adapter<DoctorProfileAdapter.DoctorProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorProfileViewHolder {
        val v: View = parent.inflate(R.layout.item_doctor_profile, false)
        return DoctorProfileViewHolder(v)
    }

    override fun onBindViewHolder(holder: DoctorProfileViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size


    class DoctorProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: String) {
            itemView.tv_item_doc_profile.text = data
        }
    }
}