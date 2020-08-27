package arunkbabu.care

import com.google.firebase.Timestamp

/**
 * Doctor's Report Data Model class. Used for doctor's report details
 */
data class DoctorReport(val reportType: Int, val reportId: String, val docName: String,
                        val reportTimestamp: Timestamp) {
    constructor() : this(Constants.NULL_INT,"","", Timestamp(0,0))
}