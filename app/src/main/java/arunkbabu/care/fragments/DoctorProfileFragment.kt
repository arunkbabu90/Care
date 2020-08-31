package arunkbabu.care.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.activities.DoctorActivity
import arunkbabu.care.adapters.DoctorProfileAdapter
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_doctor_profile.*

class DoctorProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var mAdapter: DoctorProfileAdapter
    private lateinit var mTarget: Target
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCloudStore: FirebaseStorage
    private lateinit var mDb: FirebaseFirestore

    private var isViewsLoaded: Boolean = false
    private var mDpPath = ""
    private var mName = ""
    private var mEmail = ""
    private var mRegisteredId = ""
    private var mSpeciality = ""
    private var mSex = Constants.NULL_INT
    private var mContactNo = ""
    private var mQualifications = "None"
    private var mExperience = "None"
    private var mFellowships = "None"
    private var mHospitalName = "None"

    companion object {
        var mIsUpdatesAvailable = false
        private const val REQUEST_CODE_PICK_IMAGE = 11000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewsLoaded = true
        pb_doc_profile_data.visibility = View.VISIBLE

        mAuth = FirebaseAuth.getInstance()
        mCloudStore = FirebaseStorage.getInstance()
        mDb = FirebaseFirestore.getInstance()

        var hasData: Boolean = getProfileData()
        if (!hasData) {
            // No data available. So retry in a few seconds
            Handler(Looper.getMainLooper()).postDelayed({
                hasData = getProfileData()
                if (hasData){
                    loadToViews()
                } else {
                    tv_doc_profile_err?.visibility = View.VISIBLE
                    pb_doc_profile_data?.visibility = View.GONE
                }
            }, 5000)
        } else {
            // Data loaded & available
            loadToViews()
        }

        iv_doc_profile_photo.setOnClickListener(this)
        btn_doc_sign_out.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_doc_profile_photo -> {
                val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImg.type = "image/*"
                startActivityForResult(Intent.createChooser(pickImg, getString(R.string.pick_image)), REQUEST_CODE_PICK_IMAGE)
            }
            R.id.btn_doc_sign_out -> {
                (activity as DoctorActivity).signOut()
            }
        }
    }

    /**
     * Loads all the data to the views
     */
    private fun loadToViews() {
        if (mDpPath.isNotBlank()) {
            // Load profile picture
            loadImageToView(Uri.parse(mDpPath))
        }

        tv_doc_profile_name?.text = getString(R.string.doc_name, mName)
        tv_doc_profile_speciality?.text = mSpeciality

        val profData = arrayListOf(
            "Email: $mEmail",
            "Contact No: $mContactNo",
            "Sex: ${Utils.toSexString(mSex)}",
            "Qualifications: $mQualifications",
            "Experience: $mExperience",
            "Fellowships $mFellowships",
            "Practicing Hospital: $mHospitalName",
            "Registered Id: $mRegisteredId"
        )
        mAdapter = DoctorProfileAdapter(profData)
        rv_doc_profile?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_doc_profile?.adapter = mAdapter

        pb_doc_profile_data?.visibility = View.GONE
    }

    /**
     * Helper method to get the profile data from the DoctorActivity
     * @return True if all fields has data
     */
    private fun getProfileData(): Boolean {
        if (activity != null) {
            val da: DoctorActivity = activity as DoctorActivity
            mName = da.mDoctorFullName
            mEmail = da.mEmail
            mRegisteredId = da.mRegisterId
            mQualifications = da.mQualifications
            mSpeciality = da.mSpeciality
            mSex = da.mSex
            mContactNo = da.mContactNumber
            mDpPath = da.mDoctorDpPath

            return mName.isNotBlank() && mEmail.isNotBlank() && mRegisteredId.isNotBlank()
                    && mQualifications.isNotBlank() && mSpeciality.isNotBlank() && mContactNo.isNotBlank()
        }
        return false
    }

    /**
     * Loads an image from the specified URI to the ImageView
     * @param imageUri The URI of the image to be loaded
     */
    private fun loadImageToView(@NonNull imageUri: Uri) {
        mTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                iv_doc_profile_photo?.setImageBitmap(bitmap)
                pb_doc_profile_dp?.visibility = View.GONE
                if (mIsUpdatesAvailable && bitmap != null) {
                    (activity as DoctorActivity).uploadImageFile(bitmap)
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                pb_doc_profile_dp?.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                pb_doc_profile_dp?.visibility = View.VISIBLE
            }
        }
        Picasso.get().load(imageUri).resize(960,0).into(mTarget)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null && isViewsLoaded) {
                if (Utils.isNetworkConnected(context)) {
                    pb_doc_profile_dp?.visibility = View.VISIBLE
                    mIsUpdatesAvailable = true
                    loadImageToView(imageUri = uri)
                } else {
                    Toast.makeText(context, R.string.err_internet_default, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewsLoaded = true
    }
}