package arunkbabu.care.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
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
import arunkbabu.care.dialogs.DatePickerDialog
import arunkbabu.care.dialogs.SimpleInputDialog
import arunkbabu.care.views.EditableRecyclerView
import arunkbabu.care.views.TitleRadioCardView
import com.google.android.gms.tasks.Task
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class PatientProfileFragment : Fragment(), View.OnClickListener,
    TitleRadioCardView.OnCheckedStateChangeListener, EditableRecyclerView.ItemSwipeListener,
    DatePickerDialog.DateChangeListener, SimpleInputDialog.ButtonClickListener {
    private var mUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var mDb: FirebaseFirestore? = null
    private var mCloudStore: FirebaseStorage? = null
    private var mEpochDob: Long = 0
    private var mUserType: Int = 0
    private var mAge: Int = 0
    private var mSex: Int = Constants.NULL_INT
    private var mIsUpdatesAvailable = false
    private var mFullNameClick = false
    private var mContactClick = false
    private var mHeightClick = false
    private var mWeightClick = false
    private var mFullName: String? = null
    private var mEmail: String? = null
    private var mContactNumber: String? = null
    private var mHeight: String? = null
    private var mWeight: String? = null
    private var mDob: String? = null
    private var mBmi: String? = null
    private var mImagePath: String? = null
    private var mTarget: Target? = null

    private var isViewsLoaded: Boolean = false

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 10000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
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
        mCloudStore = FirebaseStorage.getInstance()

        mUser = mAuth?.currentUser

        // Fetch the user's profile data from database

        // Fetch the user's profile data from database
        fetchData()

        iv_profile_photo.setOnClickListener(this)
        pcv_profile_full_name.setOnClickListener(this)
        pcv_profile_contact_no.setOnClickListener(this)
        pcv_profile_dob.setOnClickListener(this)
        pcv_profile_height.setOnClickListener(this)
        pcv_profile_weight.setOnClickListener(this)
        btn_profile_sign_out.setOnClickListener(this)

        isViewsLoaded = true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_profile_photo -> {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickPhotoIntent.type = "image/*"
                startActivityForResult(Intent.createChooser(pickPhotoIntent, getString(R.string.pick_image)), REQUEST_CODE_PICK_IMAGE)
            }
            R.id.pcv_profile_full_name -> {
                mFullNameClick = true
                showEditInputDialog(getString(R.string.full_name), mFullName ?: "")
            }
            R.id.pcv_profile_contact_no -> {
                mContactClick = true
                showEditInputDialog(getString(R.string.mobile_number), mContactNumber ?: "")
            }
            R.id.pcv_profile_dob -> showDatePicker()
            R.id.pcv_profile_height -> {
                mHeightClick = true
                showEditInputDialog(getString(R.string.height_desc), mHeight ?: "")
            }
            R.id.pcv_profile_weight -> {
                mWeightClick = true
                showEditInputDialog(getString(R.string.weight_desc), mWeight ?: "")
            }
            R.id.btn_profile_sign_out -> if (mAuth?.currentUser != null) {
                mAuth?.signOut()
                activity?.finish()
            }
        }
    }

    /**
     * Retrieves the user's profile data from database
     */
    private fun fetchData() {
        if (mUser != null) {
            mDb!!.collection(Constants.COLLECTION_USERS).document(mUser!!.uid).get()
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
                            mFullName = d.getString(Constants.FIELD_FULL_NAME)
                            mEmail = mUser?.email
                            mContactNumber = d.getString(Constants.FIELD_CONTACT_NUMBER)
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
                            mHeight = d.getString(Constants.FIELD_HEIGHT)
                            mWeight = d.getString(Constants.FIELD_WEIGHT)

                            // Calc BMI
                            mBmi = if (mWeight != null && mWeight != "" && mHeight != null && mHeight != "") {
                                // Calculate the bmi only if both weight and height are available
                                Utils.calculateBMI(mWeight, mHeight)
                            } else {
                                ""
                            }
                            mImagePath = d.getString(Constants.FIELD_PROFILE_PICTURE)
                            val sex = d.getLong(Constants.FIELD_SEX)
                            if (sex != null) {
                                mSex = sex.toInt()
                            }

                            // Fill the views with data
                            if (isViewsLoaded)
                                populateDataToViews()
                        } else {
                            Toast.makeText(context, getString(R.string.err_unable_to_fetch),
                                Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.err_unable_to_fetch),
                            Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }
                }
        }
    }

    /**
     * Populates the data to the Respective Views
     * Call this method only after [.fetchData] has finished fetching data from database
     */
    private fun populateDataToViews() {
        if (mFullName.isNullOrEmpty() || mEmail.isNullOrEmpty() || mContactNumber.isNullOrEmpty()) {
            fetchData()
            return
        }

        pcv_profile_full_name.bottomText = mFullName ?: ""
        pcv_profile_email.bottomText = mEmail ?: ""
        pcv_profile_contact_no.bottomText = mContactNumber ?: ""
        pcv_profile_dob.bottomText = mDob ?: ""
        if (mAge != Constants.NULL_INT) pcv_profile_age.bottomText = mAge.toString()
        if (mImagePath != null && isViewsLoaded) {
            loadImageToView(Uri.parse(mImagePath))
        }

        // Check this Radio Button only if the user has opted to provide data (ie, the database has this data)
        if (mSex == Constants.SEX_MALE) {
            rcv_profile_sex.isRadio1Checked = true
        } else if (mSex == Constants.SEX_FEMALE) {
            rcv_profile_sex.isRadio2Checked = true
        }
        pcv_profile_height.bottomText = mHeight ?: ""
        pcv_profile_weight.bottomText = mWeight ?: ""
        pcv_profile_bmi.bottomText = mBmi ?: ""


        rcv_profile_sex.setCheckChangeListener(this)
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

        if (mFullName != null && mFullName != "")
            profileData[Constants.FIELD_FULL_NAME] = mFullName ?: ""

        if (mContactNumber != null && mContactNumber != "")
            profileData[Constants.FIELD_CONTACT_NUMBER] = mContactNumber ?: ""

        if (mEpochDob != Constants.NULL_INT.toLong())
            profileData[Constants.FIELD_DOB] = mEpochDob

        if (rcv_profile_sex.isRadio1Checked)
            mSex = Constants.SEX_MALE
        else if (rcv_profile_sex.isRadio2Checked)
            mSex = Constants.SEX_FEMALE

        if (mSex != Constants.NULL_INT)
            profileData[Constants.FIELD_SEX] = mSex

        if (mHeight != null && mHeight != "")
            profileData[Constants.FIELD_HEIGHT] = mHeight ?: ""

        if (mWeight != null && mWeight != "")
            profileData[Constants.FIELD_WEIGHT] = mWeight ?: ""

        if (mUser != null) {
            mDb!!.collection(Constants.COLLECTION_USERS).document(mUser!!.uid)
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
                iv_profile_photo.setImageBitmap(bitmap)
                pb_profile_loading.visibility = View.GONE
                if (mIsUpdatesAvailable && bitmap != null) {
                    uploadImageFile(bitmap)
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                pb_profile_loading.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                pb_profile_loading.visibility = View.VISIBLE
            }
        }
        Picasso.get().load(imageUri).resize(960,0).into(mTarget as Target)
    }

    /**
     * Upload the image files selected
     * @param bitmap The image to upload
     */
    private fun uploadImageFile(bitmap: Bitmap) {
        pb_profile_loading.visibility = View.VISIBLE

        // Convert the image bitmap to InputStream
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bs = ByteArrayInputStream(bos.toByteArray())
        if (mUser != null) {
            // Upload the image file
            val uploadPath = mUser?.uid + "/" + Constants.DIRECTORY_PROFILE_PICTURE + "/" + Constants.PROFILE_PICTURE_FILE_NAME
            val storageReference = mCloudStore!!.getReference(uploadPath)
            storageReference.putStream(bs)
                .continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                    if (!task.isSuccessful) {
                        // Upload failed
                        Toast.makeText(context, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                        return@continueWithTask null
                    }
                    storageReference.downloadUrl
                }
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful && task.result != null) {
                        // Upload success; push the download URL to the database
                        val imagePath = task.result.toString()
                        mDb!!.collection(Constants.COLLECTION_USERS).document(mUser!!.uid)
                            .update(Constants.FIELD_PROFILE_PICTURE, imagePath)
                            .addOnSuccessListener { Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(context, R.string.err_upload_failed, Toast.LENGTH_SHORT).show() }
                    } else {
                        Toast.makeText(context,
                            getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show()
                    }
                    pb_profile_loading?.visibility = View.GONE
                }
        }
        mIsUpdatesAvailable = false
    }

    /**
     * Button Clicks events of the SimpleInputDialogFragment
     * @param inputText The text in the input field
     */
    override fun onPositiveButtonClick(inputText: String) {
        if (inputText != "") mIsUpdatesAvailable = true

        // Handle adding data to EditableRecyclerView
        // Handle for other views
        when {
            mFullNameClick -> {
                // First Name Click
                pcv_profile_full_name.bottomText = inputText
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
                    pcv_profile_bmi.bottomText =
                        Utils.calculateBMI(pcv_profile_weight.bottomText, inputText)
                }
                pcv_profile_height.bottomText = inputText
                mHeight = inputText
                mHeightClick = false
            }
            mWeightClick -> {
                // Weight Click
                if (pcv_profile_height.bottomText != "" && inputText != "") {
                    // Calculate BMI if height and weight are present
                    pcv_profile_bmi.bottomText =
                        Utils.calculateBMI(inputText, pcv_profile_height.bottomText)
                }
                pcv_profile_weight.bottomText = inputText
                mWeight = inputText
                mWeightClick = false
            }
        }
    }

    override fun onNegativeButtonClick() {}

    /**
     * Invoked when an item in any of the EditableRecycleView is swiped off
     * @param item The item that is swiped off
     */
    override fun onItemSwiped(item: String?) {
        mIsUpdatesAvailable = true
    }

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
            if (uri != null && isViewsLoaded) {
                loadImageToView(uri)
                mIsUpdatesAvailable = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        isViewsLoaded = false
    }
}