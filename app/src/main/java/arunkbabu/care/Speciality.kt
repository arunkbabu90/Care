package arunkbabu.care

import androidx.annotation.ColorRes

/**
 * A data class definition for doctor's speciality (Ex. Cardiology, General Medicine etc..)
 * @param id The speciality id
 * @param imageResource The image resource id
 * @param title The speciality name
 * @param description The speciality description
 * @param backgroundColor The color code of the speciality (if any). Will be applied to the UI of
 * the respective category
 */
data class Speciality(
    val id: Int,
    val imageResource: Int = R.drawable.ic_broken_image,
    val title: String = "No Title",
    val description: String = "No Description",
    @ColorRes val backgroundColor: Int = R.color.colorCatRed
) {

    companion object {
        const val GENERAL = 0
        const val ONCOLOGIST = 1
        const val PEDIATRICIAN = 2
        const val ENDOCRINOLOGIST = 3
        const val HEPATOLOGIST = 4
        const val ENT = 5
        const val UROLOGIST = 6
        const val NEPHROLOGIST = 7
        const val NEUROLOGIST = 8
        const val CARDIOLOGIST = 9
        const val PULMONOLOGIST = 10
        const val PSYCHOLOGIST = 11
        const val OPHTHALMOLOGIST = 12
        const val DENTIST = 13
        const val RHEUMATOLOGIST = 14
        const val OTHER = 15
    }
}