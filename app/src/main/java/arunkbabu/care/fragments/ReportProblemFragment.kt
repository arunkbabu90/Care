package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import arunkbabu.care.activities.OtherUntowardActivity
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.fragment_report_problem.*

/**
 * A simple [Fragment] subclass.
 */
class ReportProblemFragment : Fragment(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_problem, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_report_problem.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_report_problem -> startActivity(Intent(context, OtherUntowardActivity::class.java))
        }
    }
}