package arunkbabu.care.fragments.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.Constants
import arunkbabu.care.R
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_sign_up_main.*

/**
 * This fragment asks you to select whether you are a doctor or a patient
 */
class SignUpMainFragment : Fragment() {
    companion object {
        @JvmField
        var signUpMainFragActive = false
    }
    private var mUserType = Constants.NULL_INT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpMainFragActive = true

        rb_patient.isChecked = true
        mUserType = Constants.USER_TYPE_PATIENT
        rb_patient.setOnCheckedChangeListener { _, isChecked ->
            mUserType = if (isChecked) Constants.USER_TYPE_PATIENT else Constants.USER_TYPE_DOCTOR
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signUpMainFragActive = false
    }

    /**
     * Returns the user type currently selected by user
     */
    fun getSelectedUserType(): Int = mUserType
}