package arunkbabu.care

/**
 * Patient Data Model class. Used for storing patient details
 */
data class Patient(val patientId: String = "null",
                   val reportId: String = "null",
                   val patientName: String = "null",
                   val profilePicture: String = Constants.NULL_DP_PATH,
                   val reportType: Int = Constants.NULL_INT)