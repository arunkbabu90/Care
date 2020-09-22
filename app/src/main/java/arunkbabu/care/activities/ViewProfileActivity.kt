package arunkbabu.care.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.adapters.DoctorProfileAdapter
import arunkbabu.care.fragments.ReportProblemFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_view_profile.*
import java.util.*

class ViewProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var isViewOnlyMode = false

    private var userId = ""
    private var userType: Int = -1
    private var fullName = ""
    private var patientName = ""
    private var patientDpPath = ""
    private var profilePicturePath = ""
    private var sex = Constants.NULL_INT
    private var speciality = ""
    private var qualifications = ""
    private var fellowships = ""
    private var experience = ""
    private var workingHospitalName = ""
    private var registerId = ""
    private var age = ""
    private var height = ""
    private var weight = ""

    companion object {
        const val NAME_EXTRAS_KEY = "key_intent_extras_name"
        const val DP_EXTRAS_KEY = "key_intent_extras_doctor_profile_picture"
        const val USER_ID_EXTRAS_KEY = "key_intent_extras_document_id"
        const val USER_TYPE_EXTRAS_KEY = "user_type_extras_key"
        const val PATIENT_DP_PATH_EXTRAS_KEY = "patient_dp_extras_key"
        const val PATIENT_NAME_EXTRAS_KEY = "patient_name_extras_key"
        const val IS_VIEW_MODE_EXTRAS_KEY = "profile_is_view_mode_extras_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        mDb = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        isViewOnlyMode = intent.getBooleanExtra(IS_VIEW_MODE_EXTRAS_KEY, false)
        userType = intent.getIntExtra(USER_TYPE_EXTRAS_KEY, -1)
        userId = intent.getStringExtra(USER_ID_EXTRAS_KEY) ?: ""
        fullName = intent.getStringExtra(NAME_EXTRAS_KEY) ?: ""
        profilePicturePath = intent.getStringExtra(DP_EXTRAS_KEY) ?: ""
        patientDpPath = intent.getStringExtra(PATIENT_DP_PATH_EXTRAS_KEY) ?: ""
        patientName = intent.getStringExtra(PATIENT_NAME_EXTRAS_KEY) ?: ""


        if (profilePicturePath.isNotBlank())
            Utils.loadDpToView(this, profilePicturePath, iv_viewProfile_dp)

        if (userType == Constants.USER_TYPE_DOCTOR)
            tv_viewProfile_name?.text = getString(R.string.doc_name, fullName)
        else
            tv_viewProfile_name?.text = fullName

        fetchData()

        iv_viewProfile_dp.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_viewProfile_dp -> {
                val viewPictureIntent = Intent(this, ViewPictureActivity::class.java)
                viewPictureIntent.putExtra(ViewPictureActivity.PROFILE_PICTURE_PATH_EXTRA_KEY, profilePicturePath)
                startActivity(viewPictureIntent)
            }
            R.id.fab_viewProfile_select -> {
                // Select as Preferred Doctor
                selectDoctor()
            }
            R.id.fab_viewProfile_consult -> {
                if (PatientActivity.sReportingDoctorId != userId) {
                    selectDoctor()
                }
                val reportProblemIntent = Intent(this, OtherUntowardActivity::class.java)
                reportProblemIntent.putExtra(ReportProblemFragment.REPORTING_DOCTOR_ID_EXTRAS_KEY, userId)
                reportProblemIntent.putExtra(ReportProblemFragment.PATIENT_NAME_EXTRAS_KEY, patientName)
                reportProblemIntent.putExtra(ReportProblemFragment.PATIENT_SEX_EXTRAS_KEY, sex)
                reportProblemIntent.putExtra(ReportProblemFragment.PATIENT_DP_EXTRAS_KEY, patientDpPath)
                reportProblemIntent.putExtra(ReportProblemFragment.DOCTOR_DP_EXTRAS_KEY, profilePicturePath)
                reportProblemIntent.putExtra(ReportProblemFragment.DOCTOR_NAME_EXTRAS_KEY, fullName)
                startActivity(reportProblemIntent)
                finish()
            }
        }
    }

    /**
     * Selects the doctor
     */
    private fun selectDoctor() {
        val preferredDoc = hashMapOf(Constants.FIELD_PREFERRED_DOCTOR to userId)

        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                .set(preferredDoc, SetOptions.merge())
                .addOnSuccessListener {
                    PatientActivity.sReportingDoctorId = userId
                    PatientActivity.isNewDoctorSelected = true

                    if (SearchDoctorActivity.searchDoctorActivityActive) {
                        OtherUntowardActivity.sReportingDoctorId = userId
                        OtherUntowardActivity.sIsNewDoctorSelected = true
                        SearchDoctorActivity.instance?.finish()
                        finish()
                    }

                    // Update button state as Selected
                    fab_viewProfile_select.setIconResource(R.drawable.ic_select_filled)
                    fab_viewProfile_select.text = getString(R.string.selected)
                    Toast.makeText(this, getString(R.string.doctor_selected, fullName), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { Toast.makeText(this, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(this, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Pulls all the profile data from the database
     */
    private fun fetchData() {
        pb_viewProfile_data_loading?.visibility = View.VISIBLE

        mDb.collection(Constants.COLLECTION_USERS).document(userId).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                    if (document != null) {
                        sex = document.getLong(Constants.FIELD_SEX)?.toInt() ?: Constants.NULL_INT
                        registerId = document.getString(Constants.FIELD_DOC_REG_ID) ?: ""
                        qualifications = document.getString(Constants.FIELD_DOCTOR_QUALIFICATIONS) ?: ""
                        speciality = document.getString(Constants.FIELD_DOCTOR_SPECIALITY) ?: ""
                        fellowships = document.getString(Constants.FIELD_DOCTOR_FELLOWSHIPS) ?: ""
                        experience = document.getString(Constants.FIELD_DOCTOR_EXPERIENCE) ?: ""
                        workingHospitalName = document.getString(Constants.FIELD_WORKING_HOSPITAL_NAME) ?: ""
                        val dob = document.getLong(Constants.FIELD_DOB)
                        if (dob != null) {
                            val c = Calendar.getInstance()
                            c.timeInMillis = dob
                            age = Utils.calculateAge(
                                c[Calendar.DAY_OF_MONTH],
                                c[Calendar.MONTH],
                                c[Calendar.YEAR]
                            ).toString()
                        }
                        height = document.getString(Constants.FIELD_HEIGHT) ?: ""
                        weight = document.getString(Constants.FIELD_WEIGHT) ?: ""
                        
                        loadToViews()
                    } else {
                        Toast.makeText(this, R.string.err_unable_to_fetch, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, R.string.err_unable_to_fetch, Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Populates the all the data fetched from database to the views
     */
    private fun loadToViews() {
        tv_viewProfile_speciality?.text = speciality

        var sex: String = Utils.toSexString(sex)
        if (sex.isBlank())
            sex = "Not Provided"

        val profData: ArrayList<Pair<String, String>>
        if (userType == Constants.USER_TYPE_DOCTOR) {
            // Doctor
            if (!isViewOnlyMode) {
                fab_viewProfile_select.visibility = View.VISIBLE
                if (SearchDoctorActivity.searchDoctorActivityActive) {
                    fab_viewProfile_consult.visibility = View.GONE
                    viewProfile_guideline.setGuidelinePercent(1f)
                } else {
                    fab_viewProfile_consult.visibility = View.VISIBLE
                    fab_viewProfile_consult.setOnClickListener(this)
                }

                if (PatientActivity.sReportingDoctorId == userId) {
                    // This doctor is already the Preferred doctor. So Update button state as Selected
                    fab_viewProfile_select.setIconResource(R.drawable.ic_select_filled)
                    fab_viewProfile_select.text = getString(R.string.selected)
                } else {
                    // Enable the button
                    fab_viewProfile_select.setOnClickListener(this)
                }
            }

            if (qualifications == "") qualifications = "No Qualifications"
            if (experience == "") experience = "No Experience"
            if (fellowships == "") fellowships = "No Fellowships"
            if (workingHospitalName == "") workingHospitalName = "Not Provided"

            profData = arrayListOf(
                "Qualifications" to qualifications,
                "Experience" to experience,
                "Sex" to sex,
                "Fellowships" to fellowships,
                "Practicing Hospital" to workingHospitalName,
                "Registered Id" to registerId
            )
        } else {
            // Patient
            if (age == "") age = "Not Provided"
            if (height == "") height = "Not Provided"
            if (weight == "") weight = "Not Provided"

            profData = arrayListOf(
                "Sex" to sex,
                "Age" to age,
                "Height" to "$height cm",
                "Weight" to "$weight Kg"
            )
        }
        val adapter = DoctorProfileAdapter(profData)
        rv_viewProfile?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_viewProfile?.adapter = adapter
        Utils.runStackedRevealAnimation(this, rv_viewProfile, false)

        pb_viewProfile_data_loading?.visibility = View.GONE
    }
}