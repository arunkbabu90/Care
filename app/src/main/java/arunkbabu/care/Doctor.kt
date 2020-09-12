package arunkbabu.care

/**
 * Data Object representing a single Doctor in the Search Results list
 */
data class Doctor(var documentId: String = "",
                  val full_name: String = "",
                  val hospital: String = "",
                  val profilePicture: String = "",
                  val qualification: String = "",
                  val speciality: String = "")