package arunkbabu.care.fragments.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import com.google.android.material.transition.MaterialSharedAxis

class SignUpDoctorFragment : Fragment() {
    companion object {
        @JvmField
        var signUpDoctorFragActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpDoctorFragActive = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signUpDoctorFragActive = false
    }

    /**
     * Inspects all the EditTexts in this fragment to check whether all are filled
     * @return True  if all the fields are filled and the emails and passwords are valid; False  otherwise
     */
    fun checkAllFields(): Boolean {
        return false
    }
}