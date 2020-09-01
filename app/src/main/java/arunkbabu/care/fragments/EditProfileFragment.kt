package arunkbabu.care.fragments

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
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment() {
    var speciality = ""
    var qualifications = ""

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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editProfileFragmentActive = false
    }
}