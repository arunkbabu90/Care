package arunkbabu.care.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.fragments.DoctorSearchCategoryFragment
import arunkbabu.care.fragments.DoctorsReportFragment
import arunkbabu.care.fragments.PatientProfileFragment
import arunkbabu.care.fragments.ReportProblemFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_patient.*

class PatientActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mAccountAlreadyVerified: Boolean? = null
    private var mContactNumber: String? = null
    private var mIsLaunched = false
    private var mFragId: Int = -1

    private val REPORT_PROBLEM_FRAGMENT_ID = 9000
    private val DOC_SEARCH_FRAGMENT_ID = 9001
    private val DOCTORS_REPORT_FRAGMENT_ID = 9002
    private val PATIENT_PROFILE_FRAGMENT_ID = 9003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)

        // Set flag as Patient
        Utils.userType = Constants.USER_TYPE_PATIENT

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
                            mAccountAlreadyVerified = d.getBoolean(Constants.FIELD_ACCOUNT_VERIFIED)
                            mContactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER)
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
            R.id.menu_home -> {
                if (mFragId != REPORT_PROBLEM_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, ReportProblemFragment())
                        .commit()
                    mFragId = REPORT_PROBLEM_FRAGMENT_ID
                }
                true
            }
            R.id.menu_search -> {
                if (mFragId != DOC_SEARCH_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, DoctorSearchCategoryFragment())
                        .commit()
                    mFragId = DOC_SEARCH_FRAGMENT_ID
                }
                true
            }
            R.id.menu_doctors_report -> {
                if (mFragId != DOCTORS_REPORT_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, DoctorsReportFragment())
                        .commit()
                    mFragId = DOCTORS_REPORT_FRAGMENT_ID
                }
                true
            }
            R.id.menu_profile -> {
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
                Handler().postDelayed({
                    tv_patient_err_msg.visibility = View.GONE
                    window.statusBarColor = ContextCompat.getColor(this@PatientActivity, android.R.color.white)
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

    /**
     * Change the status bar color of PatientActivity
     */
    fun changeStatusBarColor(color: Int) {
        window.statusBarColor = color
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