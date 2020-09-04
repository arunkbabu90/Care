package arunkbabu.care

/**
 * A data class for holding the profile data
 * @param name The full name
 * @param speciality The speciality of [Constants.USER_TYPE_DOCTOR]
 * @param phone The registered contact number
 * @param sex The sex type of the user. One of [Constants.SEX_MALE], [Constants.SEX_FEMALE], [Constants.NULL_INT]
 * @param qualifications The qualifications of [Constants.USER_TYPE_DOCTOR]
 * @param experience The experience of [Constants.USER_TYPE_DOCTOR]
 * @param fellowships The fellowships of [Constants.USER_TYPE_DOCTOR]
 * @param practicingHospital The practicing hospital of [Constants.USER_TYPE_DOCTOR]
 */
data class ProfileData(val name: String = "",
                       val speciality: String = "",
                       val phone: String = "",
                       val sex: Int = Constants.NULL_INT,
                       val qualifications: String = "",
                       val experience: String = "None",
                       val fellowships: String = "None",
                       val practicingHospital: String = "None")