package arunkbabu.care.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.fragments.DoctorSearchCategoryFragment
import arunkbabu.care.fragments.DoctorsReportsFragment
import arunkbabu.care.fragments.PatientProfileFragment
import arunkbabu.care.fragments.ReportProblemFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_patient.*
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class PatientActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore
    private lateinit var mCloudStore: FirebaseStorage
    private var mAccountAlreadyVerified: Boolean? = null
    private var mIsLaunched = false
    private var mFragId = Constants.NULL_INT

    var mFullName: String = ""
    var mSex: Int = Constants.NULL_INT
    var mEmail: String = ""
    var mEpochDob: Long = 0
    var mUserType: Int = 0
    var mAge: Int = 0
    var mContactNumber: String = ""
    var mHeight: String = ""
    var mWeight: String = ""
    var mDob: String = ""
    var mBmi: String = ""
    var mPatientDpPath: String = ""

    companion object {
        private const val REPORT_PROBLEM_FRAGMENT_ID = 9000
        private const val DOC_SEARCH_FRAGMENT_ID = 9001
        private const val DOCTORS_REPORT_FRAGMENT_ID = 9002
        private const val PATIENT_PROFILE_FRAGMENT_ID = 9003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)
        // Set flag as Patient
        Utils.userType = Constants.USER_TYPE_PATIENT

        mAuth = FirebaseAuth.getInstance()
        mCloudStore = FirebaseStorage.getInstance()
        mDb = FirebaseFirestore.getInstance()
        // Add auth state listener for listening User Authentication changes like user sign-outs
        mAuth.addAuthStateListener(this)

        // Fetch the account verification status flag from the database
        val user = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                .get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val d = task.result
                        if (d != null) {
                            mAccountAlreadyVerified = d.getBoolean(Constants.FIELD_ACCOUNT_VERIFIED) ?: false
                            mContactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            if (mAccountAlreadyVerified != null && !mAccountAlreadyVerified!!) {
                                // If email NOT Already Verified; check the status again
                                checkAccountVerificationStatus()
                            } else {
                                // Silently check the verification status for security purposes
                                checkAccountVerificationSilently()
                            }
                        }
                    }
                }
        }
        fetchPatientData()

        // Load the ReportProblemFragment as default "home"
        supportFragmentManager.beginTransaction()
            .add(R.id.patient_activity_fragment_container, ReportProblemFragment())
            .commit()

        bnv_patient.setOnNavigationItemSelectedListener(this)
    }

    /**
     * Bottom navigation item selected listener
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_home -> {
                if (mFragId != REPORT_PROBLEM_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, ReportProblemFragment())
                        .commit()
                    mFragId = REPORT_PROBLEM_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_search -> {
                if (mFragId != DOC_SEARCH_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, DoctorSearchCategoryFragment())
                        .commit()
                    mFragId = DOC_SEARCH_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_doctors_report -> {
                if (mFragId != DOCTORS_REPORT_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, DoctorsReportsFragment())
                        .commit()
                    mFragId = DOCTORS_REPORT_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_profile -> {
                if (mFragId != PATIENT_PROFILE_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, PatientProfileFragment())
                        .commit()
                    mFragId = PATIENT_PROFILE_FRAGMENT_ID
                }
                true
            }
            else -> false
        }
    }

    /**
     * Retrieves the user's profile data from database
     */
    private fun fetchPatientData() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val d = task.result
                        if (d != null) {
                            // Fetch success
                            val userType = d.getLong(Constants.FIELD_USER_TYPE)
                            if (userType != null) {
                                mUserType = userType.toInt()
                                Utils.userType = mUserType
                            }
                            mFullName = d.getString(Constants.FIELD_FULL_NAME) ?: ""
                            mEmail = user.email ?: ""
                            mContactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            val dob = d.getLong(Constants.FIELD_DOB)
                            if (dob != null) {
                                mEpochDob = dob
                                mDob = Utils.convertEpochToDateString(dob)
                                val c = Calendar.getInstance()
                                c.timeInMillis = dob
                                mAge = Utils.calculateAge(c[Calendar.DAY_OF_MONTH], c[Calendar.MONTH], c[Calendar.YEAR])
                            } else {
                                mAge = Constants.NULL_INT
                                mEpochDob = Constants.NULL_INT.toLong()
                            }
                            mHeight = d.getString(Constants.FIELD_HEIGHT) ?: ""
                            mWeight = d.getString(Constants.FIELD_WEIGHT) ?: ""

                            // Calc BMI
                            mBmi = if (mWeight.isNotBlank() && mHeight.isNotBlank()) {
                                // Calculate the bmi only if both weight and height are available
                                Utils.calculateBMI(mWeight, mHeight)
                            } else {
                                ""
                            }
                            mPatientDpPath = d.getString(Constants.FIELD_PROFILE_PICTURE) ?: ""

                            val sex = d.getLong(Constants.FIELD_SEX)
                            if (sex != null) {
                                mSex = sex.toInt()
                            }
                        } else {
                            Toast.makeText(this, R.string.err_unable_to_fetch, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, R.string.err_unable_to_fetch, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Upload the image files selected
     * @param bitmap The image to upload
     */
    fun uploadImageFile(bitmap: Bitmap) {
        pb_profile_dp_loading?.visibility = View.VISIBLE

        val user: FirebaseUser? = mAuth.currentUser
        // Convert the image bitmap to InputStream
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos)
        val bs = ByteArrayInputStream(bos.toByteArray())
        if (user != null) {
            // Upload the image file
            val uploadPath = user.uid + "/" + Constants.DIRECTORY_PROFILE_PICTURE + "/" + Constants.PROFILE_PICTURE_FILE_NAME
            val storageReference = mCloudStore.getReference(uploadPath)
            storageReference.putStream(bs)
                .continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                    if (!task.isSuccessful) {
                        // Upload failed
                        Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                        return@continueWithTask null
                    }
                    storageReference.downloadUrl
                }
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful && task.result != null) {
                        // Upload success; push the download URL to the database
                        val imagePath = task.result.toString()
                        mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                            .update(Constants.FIELD_PROFILE_PICTURE, imagePath)
                            .addOnSuccessListener {
                                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                                mPatientDpPath = imagePath
                            }
                            .addOnFailureListener { Toast.makeText(this, R.string.err_upload_failed, Toast.LENGTH_SHORT).show() }
                    } else {
                        Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                    }
                    pb_profile_dp_loading?.visibility = View.GONE
                }
        }
        PatientProfileFragment.mIsUpdatesAvailable = false
    }

    /**
     * Checks whether the email associated with this account is verified silently without alerting
     * the user if email is already verified
     * This is implemented as a security measure to prevent anyone from just modifying the value in
     * the database and gain unauthorized access. If found False-positive the value is changed to
     * False again in the database
     */
    private fun checkAccountVerificationSilently() {
        // Check whether the email associated with the account is verified
        val user = mAuth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                // Email verified
                mAccountAlreadyVerified = true
                pushVerificationStatusFlag(true)
            } else {
                // Email NOT verified
                mAccountAlreadyVerified = false
                pushVerificationStatusFlag(false)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.err_account_not_verified_desc)
                tv_patient_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusUnverified))
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_patient_err_msg.isClickable = true
                tv_patient_err_msg.isFocusable = true
                tv_patient_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, mContactNumber)
                    i.putExtra(AccountVerificationActivity.KEY_BACK_BUTTON_BEHAVIOUR, AccountVerificationActivity.BEHAVIOUR_CLOSE)
                    startActivity(i)
                }
            }
        }
    }

    /**
     * Checks whether the email associated with this account is verified
     */
    private fun checkAccountVerificationStatus() {
        // Check whether the email associated with the account is verified
        val user = mAuth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                // Email verified
                mAccountAlreadyVerified = true
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusVerified)
                pushVerificationStatusFlag(true)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.account_verified)
                tv_patient_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusVerified))
                tv_patient_err_msg.isClickable = false
                tv_patient_err_msg.isFocusable = false
                Handler(Looper.getMainLooper()).postDelayed({
                    tv_patient_err_msg.visibility = View.GONE
                    window.statusBarColor = ContextCompat.getColor(this@PatientActivity, R.color.colorDarkBackgroundGrey)
                }, 3000)
            } else {
                // Email NOT verified
                mAccountAlreadyVerified = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.err_account_not_verified_desc)
                tv_patient_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusUnverified))
                tv_patient_err_msg.isClickable = true
                tv_patient_err_msg.isFocusable = true
                tv_patient_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, mContactNumber)
                    i.putExtra(AccountVerificationActivity.KEY_BACK_BUTTON_BEHAVIOUR, AccountVerificationActivity.BEHAVIOUR_CLOSE)
                    startActivity(i)
                }
            }
        }
    }

    /**
     * Helper method to push the account verified flag to the database
     * @param isVerified The flag that needs to be set
     */
    private fun pushVerificationStatusFlag(isVerified: Boolean) {
        val user = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(Constants.FIELD_ACCOUNT_VERIFIED, isVerified)
                .addOnFailureListener {
                    // Keep retrying if fails
                    pushVerificationStatusFlag(isVerified)
                }
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // User is either signed out or the login credentials no longer exists. So launch the login
        // activity again for the user to sign-in
        if (firebaseAuth.currentUser == null && !mIsLaunched) {
            mIsLaunched = true
            startActivity(Intent(this, LoginActivity::class.java))
            Toast.makeText(this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        checkAccountVerificationSilently()
    }

    override fun onResume() {
        super.onResume()
        if (mAccountAlreadyVerified != null && !mAccountAlreadyVerified!!) {
            // If email NOT Already Verified; check the status again
            checkAccountVerificationStatus()
        }
    }
}