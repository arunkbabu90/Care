package arunkbabu.care.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import arunkbabu.care.Utils
import kotlinx.android.synthetic.main.fragment_doc_search_results.*

/**
 * A simple [Fragment] subclass.
 */
class DocSearchResultsFragment(private val specialityId: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doc_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_doc_search_results_speciality.text = getString(R.string.search_res_for, Utils.toSpecialityName(specialityId))
    }
}