package arunkbabu.care.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager.widget.ViewPager
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.adapters.DocOtherUntowardPagerAdapter
import arunkbabu.care.fragments.DocAddMedicineFragment
import arunkbabu.care.fragments.DocInstructionFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_view_patient_report.*
import java.util.*
import kotlin.collections.ArrayList

class ViewPatientReportActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mPatientId: String
    private lateinit var mReportId: String
    private lateinit var mRequestId: String
    private lateinit var mDoctorName: String
    private var mReportTimestamp: Timestamp? = null
    val imagePaths: ArrayList<String> = ArrayList()
    var reportType: Int = Constants.NULL_INT
    var patientSex: Int = Constants.NULL_INT
    var reportDescription: String = ""
    var patientName: String = ""
    var patientAge: String = ""

    private var problemReportPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_patient_report)

        // Get the patient id and report type from intent
        reportType = intent.getIntExtra(Constants.PATIENT_REPORT_TYPE_KEY, Constants.NULL_INT)
        mPatientId = intent.getStringExtra(Constants.PATIENT_ID_KEY) ?: ""
        mReportId = intent.getStringExtra(Constants.PATIENT_REPORT_ID_KEY) ?: ""
        mRequestId = intent.getStringExtra(Constants.PATIENT_REQUEST_ID_KEY) ?: ""
        mDoctorName = intent.getStringExtra(Constants.DOCTOR_NAME_ID_KEY) ?: ""

        problemReportPath = (Constants.COLLECTION_USERS + "/" + mPatientId + "/" + Constants.COLLECTION_PROBLEM_REPORT)

        mDb = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        fetchPatientData()

        btn_doc_next.visibility = View.VISIBLE
    }

    /**
     * Retrieves all patient's report data and patients profile data from the database
     */
    private fun fetchPatientData() {
        val user = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(mPatientId).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val d = task.result
                        if (d != null) {
                            // Fetch success
                            patientName = d.getString(Constants.FIELD_FULL_NAME) ?: ""

                            val age = d.getLong(Constants.FIELD_DOB)
                            if (age != null) {
                                val c: Calendar = Calendar.getInstance()
                                c.timeInMillis = age
                                patientAge = Utils.calculateAge(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR)).toString()
                            } else {
                                patientAge = getString(R.string.not_provided)
                            }

                            val sex = d.getLong(Constants.FIELD_SEX)
                            if (sex != null)
                                patientSex = sex.toInt()

                            // Get the patient report details
                            fetchReportDetails(problemReportPath)
                        } else {
                            Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
        }
    }

    /**
     * Retrieve the details of the patient's report
     * @param problemReportPath The path containing the patient's report
     */
    private fun fetchReportDetails(problemReportPath: String) {
        when (reportType) {
            Constants.REPORT_TYPE_OTHER -> {
                mDb.collection(problemReportPath).document(mReportId).get()
                    .addOnCompleteListener(this) { task: Task<DocumentSnapshot> ->
                        if (task.isSuccessful) {
                            val d = task.result
                            if (d != null) {
                                // Report Fetch Success
                                reportDescription = d.getString(Constants.FIELD_PROBLEM_DESCRIPTION) ?: getString(R.string.not_provided)
                                mReportTimestamp = d.getTimestamp(Constants.FIELD_REPORT_TIMESTAMP)

                                // Retrieve all the paths of images uploaded
                                val b = d[Constants.FIELD_IMAGE_UPLOADS] as HashMap<String, String>
                                for ((_, value) in b)
                                    imagePaths.add(value)

                                loadViews()
                            } else {
                                Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
            }
        }
    }

    /**
     * Load the appropriate view based on the report type; Make the views visible and hide the ErrorTextView
     */
    private fun loadViews() {
        when (reportType) {
            Constants.REPORT_TYPE_OTHER -> {
                val otherAdapter = DocOtherUntowardPagerAdapter(supportFragmentManager, this, this)
                vp_doctor.adapter = otherAdapter
                vp_doctor.offscreenPageLimit = 3
            }
        }
        vp_doctor.addOnPageChangeListener(this)
        vp_doctor.visibility = View.VISIBLE
        btn_doc_next.visibility = View.VISIBLE
        tv_doc_error_text.visibility = View.GONE
    }

    private fun createReport() {
        Toast.makeText(this, getString(R.string.creating_report), Toast.LENGTH_SHORT).show()
        btn_doc_next.isEnabled = false
        pb_doc_next.visibility = View.VISIBLE

        val docReportPath = (Constants.COLLECTION_REPORT_DETAILS + "/" + mPatientId + "/" + Constants.COLLECTION_DOCTOR_REPORT)
        val medList = DocAddMedicineFragment.mMedicineList
        val medicines: MutableMap<String, Any> = HashMap()
        for (i in 0 until medList.size)
            medicines["med$i"] = medList[i]

        val prescription: MutableMap<String, Any> = hashMapOf(
            Constants.FIELD_FULL_NAME to mDoctorName,
            Constants.FIELD_REPORT_TYPE to reportType,
            Constants.FIELD_DOCTOR_MEDICATION_INSTRUCTIONS to DocInstructionFragment.sDocReportDescription,
            Constants.FIELD_DOCTOR_MEDICINES to medicines
        )
        // Put the time when the patient has created the report to the doctor
        // This helps the patient to identify the report when doctor sends the prescription for the patient
        if (mReportTimestamp != null) {
            prescription[Constants.FIELD_REPORT_TIMESTAMP] = mReportTimestamp!!
        } else {
            Toast.makeText(this, R.string.err_report_creation_fail, Toast.LENGTH_SHORT).show()
            fetchReportDetails(problemReportPath)
            return
        }
        mDb.collection(docReportPath).add(prescription)
            .addOnSuccessListener { documentReference: DocumentReference ->
                // Report Upload Success
                sentReportToPatient(documentReference.id)
            }
            .addOnFailureListener {
                // Report Upload Failure
                Toast.makeText(this, getString(R.string.err_report_create_fail), Toast.LENGTH_SHORT).show()
                btn_doc_next.isEnabled = true
                pb_doc_next.visibility = View.GONE
            }
    }

    private fun sentReportToPatient(reportId: String) {
        val docShortReportPath = Constants.COLLECTION_USERS + "/" + mPatientId + "/" + Constants.COLLECTION_DOCTOR_REPORT
        val shortReport: MutableMap<String, Any> = hashMapOf(
            Constants.FIELD_FULL_NAME to mDoctorName,
            Constants.FIELD_REPORT_TYPE to reportType,
            Constants.FIELD_IS_A_VALID_DOCTOR_REPORT to true,
            Constants.FIELD_REQUEST_TIMESTAMP to Timestamp.now(),
            Constants.FIELD_REPORT_ID to reportId
        )
        // Put the time when the patient has created the report to the doctor
        // This helps the patient to identify the report when doctor sends the prescription for the patient
        if (mReportTimestamp != null) {
            shortReport[Constants.FIELD_REPORT_TIMESTAMP] = mReportTimestamp!!
        } else {
            Toast.makeText(this, R.string.err_report_creation_fail, Toast.LENGTH_SHORT).show()
            fetchReportDetails(problemReportPath)
            return
        }

        mDb.collection(docShortReportPath).add(shortReport)
            .addOnSuccessListener {
                // Report Upload Success: So delete the request
                deleteRequest()
            }
            .addOnFailureListener {
                // Report Upload Failure
                Toast.makeText(this, getString(R.string.err_report_create_fail), Toast.LENGTH_SHORT).show()
                btn_doc_next.isEnabled = true
                pb_doc_next.visibility = View.GONE
            }
    }

    /**
     * Deletes the patient's request from the doctor's request list
     */
    private fun deleteRequest() {
        val user = mAuth.currentUser
        if (user != null) {
            val docRequestListPath = (Constants.COLLECTION_USERS + "/" + user.uid + "/" + Constants.COLLECTION_PATIENT_REQUEST)
            mDb.collection(docRequestListPath).document(mRequestId).delete()
                .addOnCompleteListener {
                    // Request Deletion Success
                    Toast.makeText(this, getString(R.string.report_sent, patientName), Toast.LENGTH_SHORT).show()
                    btn_doc_next.isEnabled = true
                    pb_doc_next.visibility = View.GONE
                    finish()
                }
                .addOnFailureListener {
                    // On failure try again
                    Toast.makeText(this, R.string.err_request_deletion_fail, Toast.LENGTH_LONG).show()
                    deleteRequest()
                }
        }
    }

    /**
     * Checks whether the user is currently at the last page
     * @return True if the user is at the last page
     */
    private fun isAtLastPage(): Boolean {
        return when (reportType) {
            Constants.REPORT_TYPE_OTHER -> vp_doctor.currentItem == DocOtherUntowardPagerAdapter.NUM_PAGES - 1
            else -> true
        }
    }

    /**
     * Called when the Next button is pressed
     */
    fun onDocNextPress(view: View) {
        // Hide virtual keyboard
        Utils.closeSoftInput(this)

        if (!isAtLastPage()) {
            vp_doctor.setCurrentItem(vp_doctor.currentItem + 1, true)
        } else {
            // Send the report to patient
            createReport()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
    override fun onPageScrollStateChanged(state: Int) { }
    override fun onPageSelected(position: Int) {
        // Change the button appearance and behaviour based on the pages
        when (reportType) {
            Constants.REPORT_TYPE_OTHER -> {
                when (position) {
                    3 -> {
                        btn_doc_next.text = getString(R.string.send)
                        btn_doc_next.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.send_btn_color_state_list)
                    }
                    else -> {
                        btn_doc_next.text = getString(R.string.next)
                        btn_doc_next.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list)
                    }
                }
            }
        }
    }
}