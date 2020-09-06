package arunkbabu.care.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.R
import arunkbabu.care.inflate
import kotlinx.android.synthetic.main.item_two_line_list.view.*

class DoctorProfileAdapter(private val data: ArrayList<Pair<String, String>>) : RecyclerView.Adapter<DoctorProfileAdapter.DoctorProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorProfileViewHolder {
        val v: View = parent.inflate(R.layout.item_two_line_list)
        return DoctorProfileViewHolder(v)
    }

    override fun onBindViewHolder(holder: DoctorProfileViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size


    class DoctorProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Pair<String, String>) {
            val (title, subtitle) = data
            itemView.tv_twoLineList_title.text = title
            itemView.tv_twoLineList_subtitle.text = subtitle
        }
    }
}