package arunkbabu.care.activities

import android.content.Intent
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
import arunkbabu.care.fragments.DoctorProfileFragment
import arunkbabu.care.fragments.MessageFragment
import arunkbabu.care.fragments.PatientRequestsFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_doctor.*

class DoctorActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener,
    BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore
    private var mAccountAlreadyVerified: Boolean = true
    private var mContactNumber: String = ""
    private var mIsLaunched: Boolean = false
    private var mFragId: Int = Constants.NULL_INT

    var mDoctorFullName: String = ""
    var mEmail: String = ""
    var mRegisterId: String = ""
    var mSpeciality: String = ""
    var mQualifications: String = ""
    var mSex: Int = Constants.NULL_INT

    companion object {
        private const val PATIENT_REQUESTS_FRAGMENT_ID = 8001
        private const val DOC_PRIVATE_MESSAGE_FRAGMENT_ID = 8002
        private const val DOCTOR_PROFILE_FRAGMENT_ID = 8003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        // Set flag as Patient
        Utils.userType = Constants.USER_TYPE_DOCTOR

        mAuth = FirebaseAuth.getInstance()
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
                            if (!mAccountAlreadyVerified) {
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

        fetchDoctorProfile()

        // Load the ReportProblemFragment as default "home"
        supportFragmentManager.beginTransaction()
            .add(R.id.doctor_activity_fragment_container, PatientRequestsFragment())
            .commit()

        bnv_doctor.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_requests_doc -> {
                if (mFragId != PATIENT_REQUESTS_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, PatientRequestsFragment())
                        .commit()
                    mFragId = PATIENT_REQUESTS_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_messages_doc -> {
                if (mFragId != DOC_PRIVATE_MESSAGE_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, MessageFragment())
                        .commit()
                    mFragId = DOC_PRIVATE_MESSAGE_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_profile_doc -> {
                if (mFragId != DOCTOR_PROFILE_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, DoctorProfileFragment())
                        .commit()
                    mFragId = DOCTOR_PROFILE_FRAGMENT_ID
                }
                true
            }
            else -> false
        }
    }

    /**
     * Retrieves all the profile data of the doctor
     */
    private fun fetchDoctorProfile() {
        val user = mAuth.currentUser
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document != null) {
                            mDoctorFullName = document.getString(Constants.FIELD_FULL_NAME) ?: ""
                            mEmail = user.email ?: ""
                            mContactNumber = document.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            mSex = document.getLong(Constants.FIELD_SEX)?.toInt() ?: Constants.NULL_INT
                            mRegisterId = document.getString(Constants.FIELD_REGISTRATION_NO) ?: ""
                            mQualifications = document.getString(Constants.FIELD_DOCTOR_QUALIFICATIONS) ?: ""
                            mSpeciality = document.getString(Constants.FIELD_DOCTOR_SPECIALITY) ?: ""
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
                tv_doctor_err_msg.visibility = View.VISIBLE
                tv_doctor_err_msg.setText(R.string.err_account_not_verified_desc_doc)
                tv_doctor_err_msg.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorStatusUnverified
                    )
                )
                window.statusBarColor =
                    ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_doctor_err_msg.isClickable = true
                tv_doctor_err_msg.isFocusable = true
                tv_doctor_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(
                        AccountVerificationActivity.KEY_USER_PHONE_NUMBER,
                        mContactNumber
                    )
                    i.putExtra(
                        AccountVerificationActivity.KEY_BACK_BUTTON_BEHAVIOUR,
                        AccountVerificationActivity.BEHAVIOUR_CLOSE
                    )
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
                tv_doctor_err_msg.visibility = View.VISIBLE
                tv_doctor_err_msg.setText(R.string.account_verified)
                tv_doctor_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusVerified))
                tv_doctor_err_msg.isClickable = false
                tv_doctor_err_msg.isFocusable = false
                Handler(Looper.getMainLooper()).postDelayed({
                    tv_doctor_err_msg.visibility = View.GONE
                    window.statusBarColor = ContextCompat.getColor(this@DoctorActivity, R.color.colorDarkBackgroundGrey)
                }, 3000)
            } else {
                // Email NOT verified
                mAccountAlreadyVerified = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_doctor_err_msg.visibility = View.VISIBLE
                tv_doctor_err_msg.setText(R.string.err_account_not_verified_desc_doc)
                tv_doctor_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusUnverified))
                tv_doctor_err_msg.isClickable = true
                tv_doctor_err_msg.isFocusable = true
                tv_doctor_err_msg.setOnClickListener {
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
        if (!mAccountAlreadyVerified) {
            // If email NOT Already Verified; check the status again
            checkAccountVerificationStatus()
        }
    }
}