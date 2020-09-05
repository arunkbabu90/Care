package arunkbabu.care

/**
 * Used in Search for holding the data of a Doctor in the list
 */
data class Doctor(val full_name: String = "",
                  val hospital: String = "",
                  val profilePicture: String = "",
                  val qualification: String = "",
                  val speciality: String = "")