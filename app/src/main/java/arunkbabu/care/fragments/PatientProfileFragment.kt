package arunkbabu.care.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.activities.PatientActivity
import arunkbabu.care.dialogs.DatePickerDialog
import arunkbabu.care.dialogs.SimpleInputDialog
import arunkbabu.care.views.TitleRadioCardView
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class PatientProfileFragment : Fragment(), View.OnClickListener, TitleRadioCardView.OnCheckedStateChangeListener,
    DatePickerDialog.DateChangeListener, SimpleInputDialog.ButtonClickListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseFirestore
    private var mFullNameClick = false
    private var mContactClick = false
    private var mHeightClick = false
    private var mWeightClick = false

    private var mFullName: String = ""
    private var mSex: Int = Constants.NULL_INT
    private var mEmail: String = ""
    private var mEpochDob: Long = 0
    private var mUserType: Int = 0
    private var mAge: Int = 0
    private var mContactNumber: String = ""
    private var mHeight: String = ""
    private var mWeight: String = ""
    private var mDob: String = ""
    private var mBmi: String = ""
    private var mDpPath: String = ""
    private var mTarget: Target? = null

    companion object {
        var mIsUpdatesAvailable = false
        private const val REQUEST_CODE_PICK_IMAGE = 10000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get instances
        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseFirestore.getInstance()

        var hasData: Boolean = getProfileData()
        if (!hasData) {
            // No data available. So retry in a few seconds
            Handler(Looper.getMainLooper()).postDelayed({
                hasData = getProfileData()
                if (hasData)
                    populateDataToViews()
            }, 5000)
        } else {
            // Data loaded & available
            populateDataToViews()
        }

        iv_profile_photo.setOnClickListener(this)
        tv_profile_name.setOnClickListener(this)
        pcv_profile_contact_no.setOnClickListener(this)
        pcv_profile_dob.setOnClickListener(this)
        pcv_profile_height.setOnClickListener(this)
        pcv_profile_weight.setOnClickListener(this)
        btn_profile_sign_out.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_profile_photo -> {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickPhotoIntent.type = "image/*"
                startActivityForResult(Intent.createChooser(pickPhotoIntent, getString(R.string.pick_image)), REQUEST_CODE_PICK_IMAGE)
            }
            R.id.tv_profile_name -> {
                mFullNameClick = true
                showEditInputDialog(getString(R.string.full_name), mFullName)
            }
            R.id.pcv_profile_contact_no -> {
                mContactClick = true
                showEditInputDialog(getString(R.string.mobile_number), mContactNumber)
            }
            R.id.pcv_profile_dob -> showDatePicker()
            R.id.pcv_profile_height -> {
                mHeightClick = true
                showEditInputDialog(getString(R.string.height_desc), mHeight)
            }
            R.id.pcv_profile_weight -> {
                mWeightClick = true
                showEditInputDialog(getString(R.string.weight_desc), mWeight)
            }
            R.id.btn_profile_sign_out -> if (mAuth.currentUser != null) {
                mAuth.signOut()
                activity?.finish()
            }
        }
    }

    /**
     * Populates the data to the Respective Views
     * Call this method only after [.fetchData] has finished fetching data from database
     */
    private fun populateDataToViews() {
        loadImageToView(Uri.parse(mDpPath))

        tv_profile_name.text = mFullName
        pcv_profile_email.bottomText = mEmail
        pcv_profile_contact_no.bottomText = mContactNumber
        pcv_profile_dob.bottomText = mDob
        if (mAge != Constants.NULL_INT) pcv_profile_age.bottomText = mAge.toString()

        // Check this Radio Button only if the user has opted to provide data (ie, the database has this data)
        if (mSex == Constants.SEX_MALE) {
            rcv_profile_sex.isRadio1Checked = true
        } else if (mSex == Constants.SEX_FEMALE) {
            rcv_profile_sex.isRadio2Checked = true
        }
        pcv_profile_height.bottomText = mHeight
        pcv_profile_weight.bottomText = mWeight
        pcv_profile_bmi.bottomText = mBmi

        rcv_profile_sex.setCheckChangeListener(this)
    }

    /**
     * Helper method to get the profile data from the DoctorActivity
     * @return True if all fields has data
     */
    private fun getProfileData(): Boolean {
        if (activity != null) {
            val pa = activity as PatientActivity
            mFullName = pa.mFullName
            mEmail = pa.mEmail
            mSex = pa.mSex
            mEpochDob = pa.mEpochDob
            mAge = pa.mAge
            mUserType = pa.mUserType
            mContactNumber = pa.mContactNumber
            mHeight = pa.mHeight
            mWeight = pa.mWeight
            mDob = pa.mDob
            mBmi = pa.mBmi
            mDpPath = pa.mPatientDpPath

            return mFullName.isNotBlank() && mEmail.isNotBlank() && mContactNumber.isNotBlank()
        }
        return false
    }

    /**
     * Launches the Date Picker Dialog Fragment
     */
    private fun showDatePicker() {
        val datePicker = DatePickerDialog()
        datePicker.setDateChangeListener(this)
        datePicker.show(activity?.supportFragmentManager!!, "datePicker")
    }

    /**
     * Called after selecting a date from the date picker
     * @param epoch The epoch timestamp at the selected date
     */
    override fun onDateSet(epoch: Long) {
        // Display the date in DateOfBirth EditText and also calculate the age and display it
        val formattedDate = Utils.convertEpochToDateString(epoch)
        val c = Calendar.getInstance()
        c.timeInMillis = epoch
        val age = Utils.calculateAge(c[Calendar.DAY_OF_MONTH], c[Calendar.MONTH], c[Calendar.YEAR])
        pcv_profile_dob.bottomText = formattedDate
        pcv_profile_age.bottomText = age.toString()
        mDob = formattedDate
        mEpochDob = epoch
        mAge = age
        mIsUpdatesAvailable = true
    }

    /**
     * Display the dialog for inputting data with a default text in the input field
     * @param hint The hint text to show in the dialog
     * @param defaultText The text to show in the input field
     */
    private fun showEditInputDialog(hint: String, defaultText: String) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        val ft: FragmentTransaction = activity?.supportFragmentManager!!.beginTransaction()
        val prev: Fragment? = activity?.supportFragmentManager?.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // If there is no text to show in the edit text change the button name to "Add" because
        // we are adding new data since there is no data else to "Edit" because we are editing
        // an existing data
        val buttonText = if (defaultText == "") getString(R.string.add) else getString(R.string.edit)
        val fragment = SimpleInputDialog(context, activity, hint, defaultText, buttonText)
        fragment.setButtonClickListener(this)
        fragment.show(ft, "dialog")
    }

    /**
     * Pushes all the data to the database
     */
    private fun pushToDatabase() {
        // Update profile in the database
        val profileData: MutableMap<String, Any> = HashMap()
        profileData[Constants.FIELD_USER_TYPE] = mUserType

        val pa = (activity as PatientActivity)
        val user: FirebaseUser? = mAuth.currentUser

        if (mFullName != "") {
            profileData[Constants.FIELD_FULL_NAME] = mFullName
            pa.mFullName = mFullName
        }

        if (mContactNumber != "") {
            profileData[Constants.FIELD_CONTACT_NUMBER] = mContactNumber
            pa.mContactNumber = mContactNumber
        }

        if (mEpochDob != Constants.NULL_INT.toLong()) {
            profileData[Constants.FIELD_DOB] = mEpochDob
            pa.mEpochDob = mEpochDob
        }

        if (rcv_profile_sex.isRadio1Checked)
            mSex = Constants.SEX_MALE
        else if (rcv_profile_sex.isRadio2Checked)
            mSex = Constants.SEX_FEMALE

        if (mSex != Constants.NULL_INT) {
            profileData[Constants.FIELD_SEX] = mSex
            pa.mSex = mSex
        }

        if (mHeight != "") {
            profileData[Constants.FIELD_HEIGHT] = mHeight
            pa.mHeight = mHeight
        }

        if (mWeight != "") {
            profileData[Constants.FIELD_WEIGHT] = mWeight
            pa.mWeight = mWeight
        }

        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(user.uid)
                .update(profileData)
                .addOnSuccessListener {
//                        if (context != null) {
//                            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
//                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, R.string.err_internet_default, Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Loads an image from the specified URI to the ImageView
     * @param imageUri The URI of the image to be loaded
     */
    private fun loadImageToView(@NonNull imageUri: Uri) {
        mTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                iv_profile_photo?.setImageBitmap(bitmap)
                pb_profile_dp_loading?.visibility = View.GONE
                if (mIsUpdatesAvailable && bitmap != null) {
                    (activity as PatientActivity).uploadImageFile(bitmap)
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                pb_profile_dp_loading?.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                pb_profile_dp_loading?.visibility = View.VISIBLE
            }
        }
        Picasso.get().load(imageUri).resize(960,0).into(mTarget as Target)
    }

    /**
     * Button Clicks events of the SimpleInputDialogFragment
     * @param inputText The text in the input field
     */
    override fun onPositiveButtonClick(inputText: String) {
        if (inputText != "") mIsUpdatesAvailable = true
        // Handle for other views
        when {
            mFullNameClick -> {
                // First Name Click
                tv_profile_name.text = inputText
                mFullName = inputText
                mFullNameClick = false
            }
            mContactClick -> {
                // Contact Click
                pcv_profile_contact_no.bottomText = inputText
                mContactNumber = inputText
                mContactClick = false
            }
            mHeightClick -> {
                // Height Click
                if (pcv_profile_weight.bottomText != "" && inputText != "") {
                    // Calculate BMI if height and weight are present
                    pcv_profile_bmi.bottomText = Utils.calculateBMI(pcv_profile_weight.bottomText, inputText)
                }
                pcv_profile_height.bottomText = inputText
                mHeight = inputText
                mHeightClick = false
            }
            mWeightClick -> {
                // Weight Click
                if (pcv_profile_height.bottomText != "" && inputText != "") {
                    // Calculate BMI if height and weight are present
                    pcv_profile_bmi.bottomText = Utils.calculateBMI(inputText, pcv_profile_height.bottomText)
                }
                pcv_profile_weight.bottomText = inputText
                mWeight = inputText
                mWeightClick = false
            }
        }
    }

    override fun onNegativeButtonClick() {}

    /**
     * Invoked when an item in any of the TitleRadioCardView check state changes
     * @param checked true: if checked; false: if unchecked
     * @param isRadio1 Whether checked state changed is in Radio Button 1
     */
    override fun onCheckChange(checked: Boolean, isRadio1: Boolean) {
        mIsUpdatesAvailable = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                mIsUpdatesAvailable = true
                loadImageToView(imageUri = uri)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mIsUpdatesAvailable) {
            // Push any updates to database
            pushToDatabase()
            mIsUpdatesAvailable = false
        }
    }
}