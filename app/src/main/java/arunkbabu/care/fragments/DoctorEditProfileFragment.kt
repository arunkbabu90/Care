package arunkbabu.care.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import arunkbabu.care.Constants
import arunkbabu.care.ProfileData
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.activities.DoctorActivity
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_doctor_edit_profile.*
import java.util.*

class DoctorEditProfileFragment : Fragment() {
    companion object {
        var editProfileFragmentActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editProfileFragmentActive = true

        val specialityAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.specialities, R.layout.item_dropdown_mnu)
        atv_profileEdit_speciality.setAdapter(specialityAdapter)
        atv_profileEdit_speciality.setDropDownBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDarkBackgroundGrey1)))

        val sexAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.sex, R.layout.item_dropdown_mnu)
        atv_profileEdit_sex.setAdapter(sexAdapter)
        atv_profileEdit_sex.setDropDownBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDarkBackgroundGrey1)))

        setDataToViews()

        fab_profileEdit_save.setOnClickListener {
            val data: ProfileData? = getDataFromViews()
            if (data != null) {
                // Save the profile data and press the back button
                pb_profileEdit_saving.visibility = View.VISIBLE
                (activity as DoctorActivity).onEditProfileSaveClick(data)
            }
        }
    }

    private fun setDataToViews() {
        if (activity != null) {
            val da: DoctorActivity = (activity as DoctorActivity)

            val name: String = da.doctorFullName
            val speciality: String = da.speciality
            val phone: String = da.contactNumber
            val sex: String = Utils.toSexString(da.sex)
            val qualifications: String = da.qualifications
            val experience: String = da.experience
            val fellowships: String = da.fellowships
            val hospital: String = da.workingHospitalName

            tv_profileEdit_fullName.setText(name)
            atv_profileEdit_speciality.setText(speciality)
            tv_profileEdit_phone.setText(phone)

            if (sex.isNotBlank())
                atv_profileEdit_sex.setText(sex)

            tv_profileEdit_qualifications.setText(qualifications)
            tv_profileEdit_experience.setText(experience)
            tv_profileEdit_fellowships.setText(fellowships)
            tv_profileEdit_hospital.setText(hospital)
        }
    }

    /**
     * Retrieves the values that are entered to the views
     * @return ProfileData containing all the data (name, email,..) of the profile
     */
    private fun getDataFromViews(): ProfileData? {
        val name: String = tv_profileEdit_fullName.text.toString()
        val speciality: String = atv_profileEdit_speciality.text.toString().capitalize(Locale.UK)
        val phone: String = tv_profileEdit_phone.text.toString()
        val sex: String = atv_profileEdit_sex.text.toString()
        val sexInt: Int = Utils.toSexInt(sex)
        val qualifications: String = tv_profileEdit_qualifications.text.toString()
        val experience: String = tv_profileEdit_experience.text.toString()
        val fellowships: String = tv_profileEdit_fellowships.text.toString()
        val hospital: String = tv_profileEdit_hospital.text.toString()

        val nameFilled: Boolean = name.isNotBlank()
        val qualificationFilled: Boolean = name.isNotBlank()
        val specialityFilled: Boolean = name.isNotBlank()
        val phoneFilled: Boolean = name.isNotBlank()

        if (name.isBlank())
            tv_profileEdit_fullName.error = getString(R.string.err_empty_field)

        if (qualifications.isBlank())
            tv_profileEdit_qualifications.error = getString(R.string.err_empty_field)

        if (speciality.isBlank())
            atv_profileEdit_speciality.error = getString(R.string.err_empty_field)

        if (phone.isBlank())
            tv_profileEdit_phone.error = getString(R.string.err_empty_field)

        return if ((sex.isBlank() || sexInt == Constants.SEX_FEMALE || sexInt == Constants.SEX_MALE || sexInt == Constants.NULL_INT)
            && nameFilled && qualificationFilled && specialityFilled && phoneFilled) {
            ProfileData(name, speciality, phone, sexInt, qualifications, experience, fellowships, hospital)
        } else {
            // Mandatory fields are not filled
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editProfileFragmentActive = false
    }
}