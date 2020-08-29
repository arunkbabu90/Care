package arunkbabu.care

import com.google.firebase.Timestamp

/**
 * Doctor's Report Data Model class. Used for doctor's report details
 */
data class DoctorReport(val reportType: Int = Constants.NULL_INT,
                        val reportId: String = "null",
                        val docName: String = "null",
                        val reportTimestamp: Timestamp = Timestamp.now())