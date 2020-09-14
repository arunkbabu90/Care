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
import arunkbabu.care.*
import arunkbabu.care.R
import arunkbabu.care.fragments.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_patient.*
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class PatientActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener,
    BottomNavigationView.OnNavigationItemSelectedListener, ChildEventListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cloudStore: FirebaseStorage
    private lateinit var chatRoot: DatabaseReference
    private var messageFrag: ChatsFragment? = null
    private var reportProblemFrag: ReportProblemFragment? = null
    private var accountAlreadyVerified: Boolean? = null
    private var fragId = Constants.NULL_INT
    private var isLaunched = false
    private val keys = ArrayList<String>()

    var chats = ArrayList<Chat>()
    var userId = ""
    var fullName = ""
    var sex: Int = Constants.NULL_INT
    var email = ""
    var epochDob: Long = 0
    var userType: Int = 0
    var age: Int = 0
    var contactNumber = ""
    var height = ""
    var weight = ""
    var dob = ""
    var bmi = ""
    var patientDpPath = ""
    var docName = ""
    var docDpPath = ""

    companion object {
        private const val REPORT_PROBLEM_FRAGMENT_ID = 9000
        private const val DOC_SEARCH_FRAGMENT_ID = 9001
        private const val DOCTORS_REPORT_FRAGMENT_ID = 9002
        private const val PATIENT_PROFILE_FRAGMENT_ID = 9003
        private const val PATIENT_MESSAGES_FRAGMENT_ID = 9004

        var sReportingDoctorId = ""
        var isDataLoaded = false
        var isNewDoctorSelected = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)
        // Set flag as Patient
        Utils.userType = Constants.USER_TYPE_PATIENT

        auth = FirebaseAuth.getInstance()
        cloudStore = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        // Add auth state listener for listening User Authentication changes like user sign-outs
        auth.addAuthStateListener(this)
        userId = auth.uid ?: ""

        if (userId.isNotBlank()) {
            chatRoot = Firebase.database.reference.root.child(Constants.ROOT_CHATS).child(userId)
            chatRoot.orderByChild(Constants.FIELD_CHAT_TIMESTAMP).limitToLast(20)
                .addChildEventListener(this)
        } else {
            Toast.makeText(this, getString(R.string.err_create_chat_room), Toast.LENGTH_LONG).show()
        }

        // Load the ReportProblemFragment as default "home"
        reportProblemFrag = ReportProblemFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.patient_activity_fragment_container, reportProblemFrag!!)
            .commit()

        fetchPatientData()

        bnv_patient.setOnNavigationItemSelectedListener(this)
    }

    /**
     * Bottom navigation item selected listener
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_home -> {
                if (fragId != REPORT_PROBLEM_FRAGMENT_ID) {
                    reportProblemFrag = ReportProblemFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, reportProblemFrag!!)
                        .commit()
                    fragId = REPORT_PROBLEM_FRAGMENT_ID
                    messageFrag = null
                }
                true
            }
            R.id.mnu_search -> {
                if (fragId != DOC_SEARCH_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.patient_activity_fragment_container,
                            DoctorSearchCategoryFragment()
                        )
                        .commit()
                    fragId = DOC_SEARCH_FRAGMENT_ID
                    reportProblemFrag = null
                    messageFrag = null
                }
                true
            }
            R.id.mnu_doctors_report -> {
                if (fragId != DOCTORS_REPORT_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, DoctorsReportsFragment())
                        .commit()
                    fragId = DOCTORS_REPORT_FRAGMENT_ID
                    reportProblemFrag = null
                    messageFrag = null
                }
                true
            }
            R.id.mnu_messages_patient -> {
                if (fragId != PATIENT_MESSAGES_FRAGMENT_ID) {
                    messageFrag = ChatsFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, messageFrag!!)
                        .commit()
                    fragId = PATIENT_MESSAGES_FRAGMENT_ID
                    reportProblemFrag = null
                }
                true
            }
            R.id.mnu_profile -> {
                if (fragId != PATIENT_PROFILE_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.patient_activity_fragment_container, PatientProfileFragment())
                        .commit()
                    fragId = PATIENT_PROFILE_FRAGMENT_ID
                    reportProblemFrag = null
                    messageFrag = null
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
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS).document(user.uid).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val d = task.result
                        if (d != null) {
                            // Fetch success
                            accountAlreadyVerified = d.getBoolean(Constants.FIELD_ACCOUNT_VERIFIED) ?: false
                            contactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            if (accountAlreadyVerified != null && !accountAlreadyVerified!!) {
                                // If email NOT Already Verified; check the status again
                                checkAccountVerificationStatus()
                            } else {
                                // Silently check the verification status for security purposes
                                checkAccountVerificationSilently()
                            }

                            val userType = d.getLong(Constants.FIELD_USER_TYPE)
                            if (userType != null) {
                                this.userType = userType.toInt()
                                Utils.userType = this.userType
                            }
                            fullName = d.getString(Constants.FIELD_FULL_NAME) ?: ""
                            email = user.email ?: ""
                            contactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            val dob = d.getLong(Constants.FIELD_DOB)
                            if (dob != null) {
                                epochDob = dob
                                this.dob = Utils.convertEpochToDateString(dob)
                                val c = Calendar.getInstance()
                                c.timeInMillis = dob
                                age = Utils.calculateAge(
                                    c[Calendar.DAY_OF_MONTH],
                                    c[Calendar.MONTH],
                                    c[Calendar.YEAR]
                                )
                            } else {
                                age = Constants.NULL_INT
                                epochDob = Constants.NULL_INT.toLong()
                            }
                            height = d.getString(Constants.FIELD_HEIGHT) ?: ""
                            weight = d.getString(Constants.FIELD_WEIGHT) ?: ""

                            // Calc BMI
                            bmi = if (weight.isNotBlank() && height.isNotBlank()) {
                                // Calculate the bmi only if both weight and height are available
                                Utils.calculateBMI(weight, height)
                            } else {
                                ""
                            }
                            patientDpPath = d.getString(Constants.FIELD_PROFILE_PICTURE) ?: ""
                            sReportingDoctorId = d.getString(Constants.FIELD_PREFERRED_DOCTOR) ?: ""

                            val sex = d.getLong(Constants.FIELD_SEX)
                            if (sex != null) {
                                this.sex = sex.toInt()
                            }

                            if (ReportProblemFragment.reportProblemFragmentActive)
                                reportProblemFrag?.activateViews()
                            reportProblemFrag = null
                            isDataLoaded = true

                            fetchDoctorDetails()
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
     * Fetches the doctor's details from the database
     * Includes Doctor Name: docName
     * Doctor DP: docDpPath
     */
    fun fetchDoctorDetails() {
        if (sReportingDoctorId.isNotEmpty()) {
            db.collection(Constants.COLLECTION_USERS).document(sReportingDoctorId).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val s = task.result
                        if (s != null) {
                            // Fetch success
                            docName = s.getString(Constants.FIELD_FULL_NAME) ?: ""
                            docDpPath = s.getString(Constants.FIELD_PROFILE_PICTURE) ?: ""

                            // Create a chat room with your reporting doctor
                            createChatRoomWithCurrentDoctor()
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
     * Creates a Chat Room with the currently selected doctor
     */
    private fun createChatRoomWithCurrentDoctor() {
        if (sReportingDoctorId.isNotBlank()) {
            val senderMap = hashMapOf(
                Constants.FIELD_FULL_NAME to docName,
                Constants.FIELD_PROFILE_PICTURE to docDpPath,
                Constants.FIELD_CHAT_TIMESTAMP to ServerValue.TIMESTAMP
            )
            chatRoot.child(sReportingDoctorId).updateChildren(senderMap)
        }
    }

    /**
     * Upload the image files selected
     * @param bitmap The image to upload
     */
    fun uploadImageFile(bitmap: Bitmap) {
        iv_profile_photo?.showProgressBar()
        Toast.makeText(this, R.string.uploading_photo, Toast.LENGTH_SHORT).show()

        // Convert the image bitmap to InputStream
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_QUALITY, bos)
        val bs = ByteArrayInputStream(bos.toByteArray())

        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            // Upload the image file
            val uploadPath = "${user.uid}/${Constants.DIRECTORY_PROFILE_PICTURE}/${Constants.PROFILE_PICTURE_FILE_NAME}${Constants.IMG_FORMAT_JPG}"
            val storageReference = cloudStore.getReference(uploadPath)
            storageReference.putStream(bs)
                .continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                    if (!task.isSuccessful) {
                        // Upload failed
                        Toast.makeText(
                            this,
                            getString(R.string.err_get_download_image_url),
                            Toast.LENGTH_LONG
                        ).show()
                        return@continueWithTask null
                    }
                    storageReference.downloadUrl
                }
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful && task.result != null) {
                        // Upload success; push the download URL to the database
                        val imagePath = task.result.toString()
                        db.collection(Constants.COLLECTION_USERS).document(user.uid)
                            .update(Constants.FIELD_PROFILE_PICTURE, imagePath)
                            .addOnSuccessListener {
                                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                                patientDpPath = imagePath
                            }
                            .addOnFailureListener { Toast.makeText(
                                this,
                                R.string.err_upload_failed,
                                Toast.LENGTH_SHORT
                            ).show() }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.err_get_download_image_url),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    iv_profile_photo?.hideProgressBar()
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
        val user = auth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                // Email verified
                accountAlreadyVerified = true
                pushVerificationStatusFlag(true)
            } else {
                // Email NOT verified
                accountAlreadyVerified = false
                pushVerificationStatusFlag(false)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.err_account_not_verified_desc)
                tv_patient_err_msg.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorStatusUnverified
                    )
                )
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_patient_err_msg.isClickable = true
                tv_patient_err_msg.isFocusable = true
                tv_patient_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, contactNumber)
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
        val user = auth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                // Email verified
                accountAlreadyVerified = true
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusVerified)
                pushVerificationStatusFlag(true)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.account_verified)
                tv_patient_err_msg.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorStatusVerified
                    )
                )
                tv_patient_err_msg.isClickable = false
                tv_patient_err_msg.isFocusable = false
                Handler(Looper.getMainLooper()).postDelayed({
                    tv_patient_err_msg.visibility = View.GONE
                    window.statusBarColor = ContextCompat.getColor(
                        this@PatientActivity,
                        R.color.colorDarkBackgroundGrey
                    )
                }, 3000)
            } else {
                // Email NOT verified
                accountAlreadyVerified = false
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_patient_err_msg.visibility = View.VISIBLE
                tv_patient_err_msg.setText(R.string.err_account_not_verified_desc)
                tv_patient_err_msg.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorStatusUnverified
                    )
                )
                tv_patient_err_msg.isClickable = true
                tv_patient_err_msg.isFocusable = true
                tv_patient_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, contactNumber)
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
     * Helper method to push the account verified flag to the database
     * @param isVerified The flag that needs to be set
     */
    private fun pushVerificationStatusFlag(isVerified: Boolean) {
        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(Constants.FIELD_ACCOUNT_VERIFIED, isVerified)
                .addOnFailureListener {
                    // Keep retrying if fails
                    pushVerificationStatusFlag(isVerified)
                }
        }
    }

    /**
     * Called when a Speciality-Category in DoctorSearchCategoryFragment is clicked
     * @param speciality The speciality category clicked
     */
    fun onDocCategoryClick(speciality: Speciality) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.patient_activity_fragment_container,
                DocSearchResultsFragment(speciality.id)
            )
            .commit()
    }

    /**
     * Updates the chats in Message Fragment
     */
    private fun updateChats(snapshot: DataSnapshot) {
        try {
            val key: String = snapshot.key ?: ""
            val chat: Chat = snapshot.getValue(Chat::class.java) ?: Chat()
            chat.key = key
            // Avoid repeating chat items
            if (!keys.contains(key)) {
                // If chat not exists. Just add the chat to end of list
                chats.add(chat)
                keys.add(key)
            } else {
                // If chat exists. Add new chat to end of list so that it shows on top of list
                chats.removeAt(keys.indexOf(key))
                keys.remove(key)
                chats.add(chat)
                keys.add(key)
            }
        } catch (e: DatabaseException) {
            e.printStackTrace()
        }

        if (ChatsFragment.messagesFragmentActive) {
            messageFrag?.updateData(chats)
        }
    }


    /**
     * Called when a child is added in the REAL-TIME DATABASE
     */
    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        updateChats(snapshot)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        updateChats(snapshot)
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        // Remove it from our chats array too
        try {
            val key: String = snapshot.key ?: ""
            chats.removeAt(keys.indexOf(key))
            keys.remove(key)
        } catch (e: DatabaseException) {
            e.printStackTrace()
        }

        if (ChatsFragment.messagesFragmentActive) {
            messageFrag?.updateData(chats)
        }
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }
    override fun onCancelled(error: DatabaseError) { }


    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // User is either signed out or the login credentials no longer exists. So launch the login
        // activity again for the user to sign-in
        if (firebaseAuth.currentUser == null && !isLaunched) {
            isLaunched = true
            startActivity(Intent(this, LoginActivity::class.java))
            Toast.makeText(this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onBackPressed() {
        if (DocSearchResultsFragment.docSearchResultsFragmentActive) {
            // If current Fragment is Search Results Fragment then go back to the previous fragment
            // (ie, DoctorSearchCategoryFragment) rather than exiting the app/activity
            supportFragmentManager.beginTransaction()
                .replace(R.id.patient_activity_fragment_container, DoctorSearchCategoryFragment())
                .commit()
        } else {
            // Default back button behaviour
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        checkAccountVerificationSilently()
    }

    override fun onResume() {
        super.onResume()
        if (accountAlreadyVerified != null && !accountAlreadyVerified!!) {
            // If email NOT Already Verified; check the status again
            checkAccountVerificationStatus()
        }

        if (isNewDoctorSelected)
            fetchDoctorDetails()
    }
}