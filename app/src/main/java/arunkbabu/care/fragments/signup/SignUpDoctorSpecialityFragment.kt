package arunkbabu.care.fragments.signup

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_sign_up_doctor_speciality.*

class SignUpDoctorSpecialityFragment : Fragment() {
    var speciality = ""
    var qualifications = ""

    companion object {
        @JvmField
        var signUpSpecialityFragActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_doctor_speciality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpSpecialityFragActive = true

        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.specialities, R.layout.item_dropdown_mnu)
        atv_doc_signup_speciality.setAdapter(adapter)
        atv_doc_signup_speciality.setDropDownBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDarkBackgroundGrey1)))
    }

    /**
     * Checks whether all the fields are filled
     * Returns True if filled; False otherwise
     */
    fun checkAllFields(): Boolean {
        speciality = atv_doc_signup_speciality.text.toString()
        qualifications = tv_doc_signup_qualifications.text.toString()

        val isSpecialityFilled: Boolean = speciality.isNotBlank()
        val isQualificationsFilled: Boolean = qualifications.isNotBlank()

        if (!isSpecialityFilled) {
            atv_doc_signup_speciality.error = getString(R.string.err_empty_field)
        }
        if (!isQualificationsFilled) {
            tv_doc_signup_qualifications.error = getString(R.string.err_empty_field)
        }

        return  isSpecialityFilled && isQualificationsFilled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signUpSpecialityFragActive = false
    }
}