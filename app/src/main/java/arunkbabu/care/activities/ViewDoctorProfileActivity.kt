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
import arunkbabu.care.fragments.DocSearchResultsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_view_doctor_profile.*

class ViewDoctorProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var mUserId = ""
    private var mFullName = ""
    private var mProfilePicturePath = ""
    private var mSex = Constants.NULL_INT
    private var mSpeciality = ""
    private var mQualifications = ""
    private var mFellowships = ""
    private var mExperience = ""
    private var mWorkingHospitalName = ""
    private var mRegisterId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_doctor_profile)

        mDb = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        mUserId = intent.getStringExtra(DocSearchResultsFragment.USER_ID_EXTRAS_KEY) ?: ""
        mFullName = intent.getStringExtra(DocSearchResultsFragment.DOCTOR_NAME_EXTRAS_KEY) ?: ""
        mProfilePicturePath = intent.getStringExtra(DocSearchResultsFragment.DOCTOR_DP_EXTRAS_KEY) ?: ""

        if (mProfilePicturePath.isNotBlank())
            Utils.loadDpToView(this, mProfilePicturePath, iv_viewProfile_dp)
        tv_viewProfile_name.text = mFullName
        fetchData()

        if (PatientActivity.sReportingDoctorId == mUserId) {
            // This doctor is already the Preferred doctor. So Update button state as Selected
            fab_viewProfile_select.setIconResource(R.drawable.ic_select_filled)
            fab_viewProfile_select.text = getString(R.string.selected)
        } else {
            // Enable the button
            fab_viewProfile_select.setOnClickListener(this)
        }

        iv_viewProfile_dp.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_viewProfile_dp -> {
                val viewPictureIntent = Intent(this, ViewPictureActivity::class.java)
                viewPictureIntent.putExtra(ViewPictureActivity.PROFILE_PICTURE_PATH_EXTRA_KEY, mProfilePicturePath)
                startActivity(viewPictureIntent)
            }
            R.id.fab_viewProfile_select -> {
                // Select as Preferred Doctor
                val preferredDoc = hashMapOf(Constants.FIELD_PREFERRED_DOCTOR to mUserId)

                val user: FirebaseUser? = mAuth.currentUser
                if (user != null) {
                    mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                        .set(preferredDoc, SetOptions.merge())
                        .addOnSuccessListener {
                            PatientActivity.sReportingDoctorId = mUserId
                            PatientActivity.isNewDoctorSelected = true

                            // Update button state as Selected
                            fab_viewProfile_select.setIconResource(R.drawable.ic_select_filled)
                            fab_viewProfile_select.text = getString(R.string.selected)
                            Toast.makeText(this, getString(R.string.doctor_selected, mFullName), Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { Toast.makeText(this, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show() }
                } else {
                    Toast.makeText(this, getString(R.string.err_failed_set_preferred_doctor), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Pulls all the profile data from the database
     */
    private fun fetchData() {
        pb_viewProfile_data_loading?.visibility = View.VISIBLE

        mDb.collection(Constants.COLLECTION_USERS).document(mUserId).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                    if (document != null) {
                        mSex = document.getLong(Constants.FIELD_SEX)?.toInt() ?: Constants.NULL_INT
                        mRegisterId = document.getString(Constants.FIELD_DOC_REG_ID) ?: ""
                        mQualifications = document.getString(Constants.FIELD_DOCTOR_QUALIFICATIONS) ?: ""
                        mSpeciality = document.getString(Constants.FIELD_DOCTOR_SPECIALITY) ?: ""
                        mFellowships = document.getString(Constants.FIELD_DOCTOR_FELLOWSHIPS) ?: ""
                        mExperience = document.getString(Constants.FIELD_DOCTOR_EXPERIENCE) ?: ""
                        mWorkingHospitalName = document.getString(Constants.FIELD_WORKING_HOSPITAL_NAME) ?: ""
                        
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
        tv_viewProfile_speciality?.text = mSpeciality

        var sex: String = Utils.toSexString(mSex)
        if (sex.isBlank())
            sex = "Not Provided"

        val profData = arrayListOf(
            "Qualifications" to mQualifications,
            "Experience" to mExperience,
            "Sex" to sex,
            "Fellowships" to mFellowships,
            "Practicing Hospital" to mWorkingHospitalName,
            "Registered Id" to mRegisterId
        )
        val adapter = DoctorProfileAdapter(profData)
        rv_viewProfile?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_viewProfile?.adapter = adapter
        Utils.runLayoutAnimation(this, rv_viewProfile, false)

        pb_viewProfile_data_loading?.visibility = View.GONE
    }
}