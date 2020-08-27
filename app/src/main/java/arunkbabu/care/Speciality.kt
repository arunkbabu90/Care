package arunkbabu.care

import androidx.annotation.ColorRes

/**
 * A data class definition for doctor's speciality (Ex. Cardiology, General Medicine etc..)
 * @param id The speciality id.
 * Refer [Constants.SPECIALITY_GENERAL], [Constants.SPECIALITY_CARDIOLOGIST], [Constants.SPECIALITY_PEDIATRICIAN],
 * [Constants.SPECIALITY_DENTIST], [Constants.SPECIALITY_ENDOCRINOLOGIST], [Constants.SPECIALITY_ENT],
 * [Constants.SPECIALITY_HEPATOLOGIST], [Constants.SPECIALITY_NEPHROLOGIST], [Constants.SPECIALITY_NEUROLOGIST],
 * [Constants.SPECIALITY_ONCOLOGIST], [Constants.SPECIALITY_OPHTHALMOLOGIST], [Constants.SPECIALITY_UROLOGIST],
 * [Constants.SPECIALITY_PSYCHOLOGIST], [Constants.SPECIALITY_PULMONOLOGIST], [Constants.SPECIALITY_RHEUMATOLOGIST],
 * [Constants.SPECIALITY_OTHER]
 * @param imageResource The image resource id
 * @param title The speciality name
 * @param description The speciality description
 * @param backgroundColor The color code of the speciality (if any). Will be applied to the UI of
 * the respective category
 */
data class Speciality(val id: Int,
                      val imageResource: Int = R.drawable.ic_broken_image,
                      val title: String = "No Title",
                      val description: String = "No Description",
                      @ColorRes val backgroundColor: Int = R.color.colorCatRed)