package arunkbabu.care.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.*
import arunkbabu.care.R
import arunkbabu.care.fragments.ChatsFragment
import arunkbabu.care.fragments.DoctorEditProfileFragment
import arunkbabu.care.fragments.DoctorProfileFragment
import arunkbabu.care.fragments.PatientRequestsFragment
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
import kotlinx.android.synthetic.main.activity_doctor.*
import kotlinx.android.synthetic.main.fragment_doctor_profile.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class DoctorActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener,
    BottomNavigationView.OnNavigationItemSelectedListener, ChildEventListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cloudStore: FirebaseStorage
    private lateinit var chatRoot: DatabaseReference
    private lateinit var connectivityManager: ConnectivityManager
    private var chatsFrag: ChatsFragment? = null
    private var isAccountAlreadyVerified = true
    private var isLaunched = false
    private var fragId = Constants.NULL_INT
    private val keys = ArrayList<String>()

    var chats = ArrayList<Chat>()
    var userId = ""
    var doctorDpPath = ""
    var doctorFullName = ""
    var contactNumber = ""
    var email = ""
    var registerId = ""
    var speciality = ""
    var qualifications = ""
    var sex = Constants.NULL_INT
    var fellowships = ""
    var experience = ""
    var workingHospitalName = ""

    val TAG: String = DoctorActivity::class.java.simpleName

    companion object {
        private const val PATIENT_REQUESTS_FRAGMENT_ID = 8001
        private const val DOC_PRIVATE_MESSAGE_FRAGMENT_ID = 8002
        private const val DOCTOR_PROFILE_FRAGMENT_ID = 8003

        var isInternetConnected = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)
        // Set flag as Doctor
        Utils.userType = Constants.USER_TYPE_DOCTOR

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkChangeCallback()

        Firebase.database.setPersistenceEnabled(true)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        cloudStore = FirebaseStorage.getInstance()
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

        // Fetch the account verification status flag from the database
        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS).document(user.uid)
                .get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val d = task.result
                        if (d != null) {
                            isAccountAlreadyVerified = d.getBoolean(Constants.FIELD_ACCOUNT_VERIFIED) ?: false
                            contactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            if (!isAccountAlreadyVerified) {
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
                if (fragId != PATIENT_REQUESTS_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, PatientRequestsFragment())
                        .commit()
                    fragId = PATIENT_REQUESTS_FRAGMENT_ID
                    chatsFrag = null
                }
                true
            }
            R.id.mnu_messages_doc -> {
                if (fragId != DOC_PRIVATE_MESSAGE_FRAGMENT_ID) {
                    chatsFrag = ChatsFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, chatsFrag!!)
                        .commit()
                    fragId = DOC_PRIVATE_MESSAGE_FRAGMENT_ID
                }
                true
            }
            R.id.mnu_profile_doc -> {
                if (fragId != DOCTOR_PROFILE_FRAGMENT_ID) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, DoctorProfileFragment())
                        .commit()
                    fragId = DOCTOR_PROFILE_FRAGMENT_ID
                    chatsFrag = null
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
        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS).document(user.uid).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document != null) {
                            doctorFullName = document.getString(Constants.FIELD_FULL_NAME) ?: ""
                            doctorDpPath= document.getString(Constants.FIELD_PROFILE_PICTURE) ?: ""
                            email = user.email ?: ""
                            contactNumber = document.getString(Constants.FIELD_CONTACT_NUMBER) ?: ""
                            sex = document.getLong(Constants.FIELD_SEX)?.toInt() ?: Constants.NULL_INT
                            registerId = document.getString(Constants.FIELD_DOC_REG_ID) ?: ""
                            qualifications = document.getString(Constants.FIELD_DOCTOR_QUALIFICATIONS) ?: ""
                            speciality = document.getString(Constants.FIELD_DOCTOR_SPECIALITY) ?: ""
                            fellowships = document.getString(Constants.FIELD_DOCTOR_FELLOWSHIPS) ?: ""
                            experience = document.getString(Constants.FIELD_DOCTOR_EXPERIENCE) ?: ""
                            workingHospitalName = document.getString(Constants.FIELD_WORKING_HOSPITAL_NAME) ?: ""
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
     * Pushes all the profile data to database
     * @param pressBackOnSave Whether to automatically press the back button after saving
     */
    private fun pushToDatabase(pressBackOnSave: Boolean) {
        val pd: HashMap<String, Any> = HashMap()
        val dl: HashMap<String, Any> = HashMap()

        val user: FirebaseUser? = auth.currentUser

        if (user != null) {
            if (doctorFullName.isNotBlank()) {
                pd[Constants.FIELD_FULL_NAME] = doctorFullName
                dl[Constants.FIELD_FULL_NAME] = doctorFullName
                dl[Constants.FIELD_SEARCH_NAME] = doctorFullName.toLowerCase(Locale.UK)
            }

            if (contactNumber.isNotBlank())
                pd[Constants.FIELD_CONTACT_NUMBER] = contactNumber

            if (sex != Constants.NULL_INT)
                pd[Constants.FIELD_SEX] = sex

            if (registerId.isNotBlank())
                pd[Constants.FIELD_DOC_REG_ID] = registerId

            if (qualifications.isNotBlank()) {
                pd[Constants.FIELD_DOCTOR_QUALIFICATIONS] = qualifications
                dl[Constants.FIELD_DOCTOR_QUALIFICATIONS] = qualifications
            }

            if (speciality.isNotBlank()) {
                pd[Constants.FIELD_DOCTOR_SPECIALITY] = speciality
                dl[Constants.FIELD_DOCTOR_SPECIALITY] = speciality
            }

            if (fellowships.isNotBlank())
                pd[Constants.FIELD_DOCTOR_FELLOWSHIPS] = fellowships

            if (experience.isNotBlank())
                pd[Constants.FIELD_DOCTOR_EXPERIENCE] = experience

            if (workingHospitalName.isNotBlank()) {
                pd[Constants.FIELD_WORKING_HOSPITAL_NAME] = workingHospitalName
                dl[Constants.FIELD_WORKING_HOSPITAL_NAME] = workingHospitalName
            }

            db.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(pd)
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                    if (pressBackOnSave) {
                        // Press back button
                        onBackPressed()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.err_internet_default, Toast.LENGTH_SHORT).show()
                }

            if (dl.isNotEmpty()) {
                db.collection(Constants.COLLECTION_DOCTORS_LIST).document(user.uid)
                    .update(dl)
                    .addOnSuccessListener { Log.d(TAG, "Save to Doctor List Success") }
                    .addOnFailureListener { Log.d(TAG, "Save to Doctor List Failure") }
            }
        } else {
            Toast.makeText(this, R.string.err_internet_default, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Upload the image files selected
     * @param bitmap The image to upload
     */
    fun uploadImageFile(bitmap: Bitmap) {
        iv_doc_profile_photo?.showProgressBar()
        Toast.makeText(this, R.string.uploading_photo, Toast.LENGTH_SHORT).show()

        // Convert the image bitmap to InputStream
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_QUALITY, bos)
        val bs = ByteArrayInputStream(bos.toByteArray())

        val user = auth.currentUser
        if (user != null) {
            // Upload the image file
            val uploadPath = "${user.uid}/${Constants.DIRECTORY_PROFILE_PICTURE}/${Constants.PROFILE_PICTURE_FILE_NAME}${Constants.IMG_FORMAT_JPG}"
            val storageReference = cloudStore.getReference(uploadPath)
            storageReference.putStream(bs).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                    if (!task.isSuccessful) {
                        // Upload failed
                        Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                        return@continueWithTask null
                    }
                    storageReference.downloadUrl
                }
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful && task.result != null) {
                        // Upload success; push the download URL to the database, also update the mDoctorDpPath
                        val imagePath = task.result.toString()
                        doctorDpPath = imagePath
                        db.collection(Constants.COLLECTION_USERS).document(user.uid)
                            .update(Constants.FIELD_PROFILE_PICTURE, imagePath)
                            .addOnSuccessListener { Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(this, R.string.err_upload_failed, Toast.LENGTH_SHORT).show() }

                        db.collection(Constants.COLLECTION_DOCTORS_LIST).document(user.uid)
                            .update(Constants.FIELD_PROFILE_PICTURE, imagePath)
                            .addOnSuccessListener { Log.d(TAG, "Dp path push to Doctor List Success") }
                            .addOnFailureListener { Log.d(TAG, "Dp path push to Doctor List Failure") }
                    } else {
                        Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                    }
                    iv_doc_profile_photo?.hideProgressBar()
                }
        }
        DoctorProfileFragment.mIsUpdatesAvailable = false
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
                isAccountAlreadyVerified = true
                pushVerificationStatusFlag(true)
            } else {
                // Email NOT verified
                isAccountAlreadyVerified = false
                pushVerificationStatusFlag(false)
                tv_doctor_err_msg.visibility = View.VISIBLE
                tv_doctor_err_msg.setText(R.string.err_account_not_verified_desc_doc)
                tv_doctor_err_msg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStatusUnverified))
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorStatusUnverified)
                tv_doctor_err_msg.isClickable = true
                tv_doctor_err_msg.isFocusable = true
                tv_doctor_err_msg.setOnClickListener {
                    // Launch the Verification Activity
                    val i = Intent(this, AccountVerificationActivity::class.java)
                    i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, user.email)
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, contactNumber)
                    i.putExtra(AccountVerificationActivity.KEY_BACK_BUTTON_BEHAVIOUR, AccountVerificationActivity.BEHAVIOUR_CLOSE)
                    startActivity(i)
                }
            }
        }
    }

    /**
     * Register a callback to be invoked when network connectivity changes
     * @return True If internet is available; False otherwise
     */
    private fun registerNetworkChangeCallback(): Boolean {
        val isAvailable = BooleanArray(1)
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Internet is Available
                runOnUiThread {
                    isInternetConnected = true
                    isAvailable[0] = true
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Internet is Unavailable
                isAvailable[0] = false
                runOnUiThread {
                    isInternetConnected = false
                }
            }
        })
        return isAvailable[0]
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
                isAccountAlreadyVerified = true
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
                isAccountAlreadyVerified = false
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
                    i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, contactNumber)
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
        val user = auth.currentUser
        if (user != null) {
            db.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(Constants.FIELD_ACCOUNT_VERIFIED, isVerified)
                .addOnFailureListener {
                    // Keep retrying if fails
                    pushVerificationStatusFlag(isVerified)
                }
            // Also push to DoctorList Collection for indexing
            db.collection(Constants.COLLECTION_DOCTORS_LIST).document(user.uid)
                .update(Constants.FIELD_ACCOUNT_VERIFIED, isVerified)
                .addOnFailureListener {
                    Log.d(TAG, "Failed to Add Verification Status Flag to DoctorsList Collection")
                }
        }
    }

    /**
     * Launches the DoctorEditProfileFragment
     */
    fun launchEditProfileFragment() {
        val editProfFrag = DoctorEditProfileFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.doctor_activity_fragment_container, editProfFrag, editProfFrag.tag)
            .addToBackStack(editProfFrag.tag)
            .commit()
    }


    /**
     * SignOut of your account
     */
    fun signOut() {
        auth.signOut()
        finish()
    }

    /**
     * Called when the Save Button in the DoctorEditProfileFragment is clicked
     */
    fun onEditProfileSaveClick(data: ProfileData) {
        // Save the data values if not empty; to this activity and push them to database
        if (data.name.isNotBlank() && data.name != "null")
            doctorFullName = data.name

        if (data.phone.isNotBlank() && data.phone != "null")
            contactNumber = data.phone

        if (data.speciality.isNotBlank() && data.speciality != "null")
            speciality = data.speciality

        if (data.qualifications.isNotBlank() && data.qualifications != "null")
            qualifications = data.qualifications

        if (data.sex != Constants.NULL_INT)
            sex = data.sex

        if (data.fellowships.isNotBlank() && data.fellowships != "null")
            fellowships = data.fellowships

        if (data.experience.isNotBlank() && data.experience != "null")
            experience = data.experience

        if (data.practicingHospital.isNotBlank() && data.practicingHospital != "null")
            workingHospitalName = data.practicingHospital

        pushToDatabase(pressBackOnSave = true)
    }

    /**
     * Updates the chats in ChatsFragment
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
                // If chat exists. Add new chat to end of list so that it shows at the TOP of list
                chats.removeAt(keys.indexOf(key))
                keys.remove(key)
                chats.add(chat)
                keys.add(key)
            }
        } catch (e: DatabaseException) {
            e.printStackTrace()
        }

        if (ChatsFragment.isFragmentActive) {
            chatsFrag?.updateData(chats)
        }
    }

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

    override fun onPause() {
        super.onPause()
        checkAccountVerificationSilently()
    }

    override fun onResume() {
        super.onResume()
        if (!isAccountAlreadyVerified) {
            // If email NOT Already Verified; check the status again
            checkAccountVerificationStatus()
        }
    }

    override fun onBackPressed() {
        if (DoctorEditProfileFragment.editProfileFragmentActive) {
            // If DoctorEditProfileFragment is active go back to the previous fragment rather than exiting the activity
            when (Utils.userType) {
                Constants.USER_TYPE_DOCTOR -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.doctor_activity_fragment_container, DoctorProfileFragment())
                        .commit()
                }
                Constants.USER_TYPE_PATIENT -> {  }
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        updateChats(snapshot)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        updateChats(snapshot)
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        // Remove the chat entry from the ChatsFragment too
        try {
            val key: String = snapshot.key ?: ""
            chats.removeAt(keys.indexOf(key))
            keys.remove(key)
        } catch (e: DatabaseException) {
            e.printStackTrace()
        }

        if (ChatsFragment.isFragmentActive) {
            chatsFrag?.updateData(chats)
        }
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

    override fun onCancelled(error: DatabaseError) { }
}