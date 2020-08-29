package arunkbabu.care.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.adapters.OtherUntowardPagerAdapter;
import arunkbabu.care.fragments.PatientReportDescriptionFragment;
import arunkbabu.care.fragments.UploadFileFragment;
import arunkbabu.care.views.CircularImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OtherUntowardActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.tv_otheruntoward_error) TextView mErrorTextView;
    @BindView(R.id.tv_otheruntoward_doctor_name) TextView mDocNameTextView;
    @BindView(R.id.iv_otheruntoward_doctor_dp) CircularImageView mDocDpImageView;
    @BindView(R.id.vp_otheruntoward) ViewPager mPager;
    @BindView(R.id.btn_otheruntoward_next) MaterialButton mNextButton;
    @BindView(R.id.pb_otheruntoward) ProgressBar mProgressBar;
    @BindView(R.id.otheruntoward_reporting_doctor) TextView mReportingDoctorTextView;

    private OtherUntowardPagerAdapter mAdapter;
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private FirebaseStorage mCloudStore;
    private FirebaseUser mUser;

    private static String sReportingDoctorId;
    private static String sDocName;
    private static String sDocDpPath;
    private static String sPatientDpPath;
    private static int sPatientSex;
    private static String sPatientName;

    private ArrayList<Uri> mDownloadURIs;
    private boolean mIsNetworkConnected;
    private boolean mIsAccountAlreadyVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_untoward);
        ButterKnife.bind(this);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCloudStore = FirebaseStorage.getInstance();
        mUser = mAuth.getCurrentUser();

        mDownloadURIs = new ArrayList<>();

        // Register a callback to be invoked when the network connectivity changes
        addNetworkStateCallback();

        // Fetch the account verification status flag from the database
        if (mUser != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(mUser.getUid())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot d = task.getResult();
                    if (d != null) {
                        Boolean isVerified = d.getBoolean(Constants.FIELD_ACCOUNT_VERIFIED);
                        if (isVerified != null) {
                            mIsAccountAlreadyVerified = isVerified;
                        }
                    }
                }
            });
        }

        // TODO: Set reporting doctor id here
        sReportingDoctorId = "67mSInWNAbZkw8oDpBS98OOeeEK2";
        // Fetch the name, dp of the doctor from database
        fetchDoctorDetails();
    }

    /**
     * Fetches the doctor's details from the database
     * Includes Doctor Name: sDocName
     *          Doctor DP: sDocDpPath
     */
    private void fetchDoctorDetails() {
        mDb.collection(Constants.COLLECTION_USERS).document(sReportingDoctorId)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot s = task.getResult();
                if (s != null) {
                    // Fetch success
                    sDocName = s.getString(Constants.FIELD_FULL_NAME);
                    sDocDpPath = s.getString(Constants.FIELD_PROFILE_PICTURE);
                    mDocNameTextView.setText((sDocName == null || sDocName.equals("")) ? getString(R.string.not_set) : sDocName);

                    // Fetch the dp, sex of the patient from database
                    fetchPatientDetails();
                    // Hide the Loading message & Show the Constipation layout
                    loadViews();
                } else {
                    Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Fetches the patient's details from the database
     * Includes Patient DP: sPatientDpPath
     *          Patient Sex: sPatientSex
     */
    private void fetchPatientDetails() {
        if (mUser != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(mUser.getUid())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot s = task.getResult();
                    if (s != null) {
                        // Fetch success
                        sPatientDpPath = s.getString(Constants.FIELD_PROFILE_PICTURE);
                        sPatientName = s.getString(Constants.FIELD_FULL_NAME);
                        Long sex = s.getLong(Constants.FIELD_SEX);
                        if (sex != null) {
                            sPatientSex = sex.intValue();
                        }
                        if (sPatientSex != 0 && sPatientSex != 1) {
                            sPatientSex = Constants.SEX_MALE;
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    /**
     * Upload the image files selected
     */
    private void uploadImageFiles() {
        mNextButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);

        if (mUser != null) {
            Toast.makeText(this, getString(R.string.uploading_files), Toast.LENGTH_SHORT).show();
            // Upload the image files
            ArrayList<Uri> imgPaths = UploadFileFragment.sPathList;
            ArrayList<String> fileNames = UploadFileFragment.sFileNameList;

            for (int i = 0; i < imgPaths.size(); i++) {
                // Upload all file one by one
                String uploadPath = mUser.getUid() + "/" + Constants.DIRECTORY_SENT_IMAGES + fileNames.get(i);

                StorageReference storageReference = mCloudStore.getReference(uploadPath);
                storageReference.putFile(imgPaths.get(i))
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                // Upload failed
                                Toast.makeText(this,
                                        getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show();
                                return null;
                            }
                            return storageReference.getDownloadUrl();
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mDownloadURIs.add(task.getResult());
                                if (mDownloadURIs.size() == UploadFileFragment.sPathList.size()) {
                                    // All files successfully uploaded; so start creating report
                                    createReport();
                                }
                            } else {
                                Toast.makeText(this,
                                        getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
    }

    /**
     * Pushes the report to database
     */
    private void createReport() {
        if (mUser != null) {
            Toast.makeText(this, getString(R.string.creating_report), Toast.LENGTH_SHORT).show();
            String problemReportPath = Constants.COLLECTION_USERS + "/" + mUser.getUid() + "/" + Constants.COLLECTION_PROBLEM_REPORT;

            Map<String, Object> downloadUrls = new HashMap<>();
            for (int i = 0; i < mDownloadURIs.size(); i++) {
                // Stringify the URI for uploading to db; Otherwise it won't work
                String url = (mDownloadURIs.get(i)!= null) ? mDownloadURIs.get(i).toString() : "";
                downloadUrls.put("img" + i, url);
            }

            Map<String, Object> reportData = new HashMap<>();
            reportData.put(Constants.FIELD_PROBLEM_DESCRIPTION, PatientReportDescriptionFragment.sReportDescription);
            reportData.put(Constants.FIELD_IMAGE_UPLOADS, downloadUrls);
            reportData.put(Constants.FIELD_REPORT_TIMESTAMP, Timestamp.now());

            mDb.collection(problemReportPath).add(reportData)
                    .addOnSuccessListener(documentReference -> {
                        // Report Creation Success
                        sendRequestToDoctor(documentReference.getId());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.err_report_create_fail), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Step 3: Send the report to doctor
     * @param reportId The String id of the report
     */
    private void sendRequestToDoctor(String reportId) {
        String docRequestListPath = Constants.COLLECTION_USERS + "/"
                + sReportingDoctorId + "/" + Constants.COLLECTION_PATIENT_REQUEST;
        if (mUser != null) {
            // Get the user's id
            String userId = mUser.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put(Constants.FIELD_PATIENT_ID, userId);
            data.put(Constants.FIELD_IS_A_VALID_REQUEST, true);
            data.put(Constants.FIELD_PATIENT_NAME, sPatientName);
            data.put(Constants.FIELD_PROFILE_PICTURE, sPatientDpPath);
            data.put(Constants.FIELD_REPORT_TYPE, Constants.REPORT_TYPE_OTHER);
            data.put(Constants.FIELD_REPORT_ID, reportId);
            data.put(Constants.FIELD_REQUEST_TIMESTAMP, Timestamp.now());

            mDb.collection(docRequestListPath).add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this,
                                getString(R.string.request_sent, sDocName), Toast.LENGTH_LONG).show();
                        mNextButton.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                getString(R.string.err_report_creation_fail), Toast.LENGTH_SHORT).show();
                        mNextButton.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    });

            finish();
        } else {
            Toast.makeText(this, getString(R.string.err_user_not_logged_in), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Make the views visible & hide the ErrorTextView
     */
    private void loadViews() {
        mAdapter = new OtherUntowardPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(this);

        mNextButton.setVisibility(View.VISIBLE);
        mDocNameTextView.setVisibility(View.VISIBLE);
        mDocDpImageView.setVisibility(View.VISIBLE);
        mPager.setVisibility(View.VISIBLE);
        mReportingDoctorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
    }

    /**
     * Register a callback for observing network connectivity changes
     */
    private void addNetworkStateCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    // Internet is Available
                    mIsNetworkConnected = true;
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    // Internet is Unavailable
                    mIsNetworkConnected = false;
                }
            });
        }
    }

    /**
     * Define Next Button Click behaviour
     * @param view The current button view
     */
    public void onNextPress(View view) {
        // Hide virtual keyboard
        Utils.closeSoftInput(this);

        if (!isAtLastPage()) {
            // NEXT Button
            mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        } else {
            // Document is at last page. So create a report and send it to the doctor
            // SEND button
            if (mIsNetworkConnected) {
                // Internet Available
                if (mIsAccountAlreadyVerified) {
                    // Only SEND if account is VERIFIED
                    if (UploadFileFragment.sPathList != null && !UploadFileFragment.sPathList.isEmpty()) {
                        // Only upload the images if they are present or selected for upload
                        // In other-words, if there are no images don't upload it
                        uploadImageFiles();
                    } else {
                        // No files to upload; so start creating report immediately
                        createReport();
                        mNextButton.setEnabled(false);
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    Utils.showErrorDialog(this, getString(R.string.err_account_not_verified_desc2),
                            "", getString(R.string.close));
                }
            } else {
                // Internet Unavailable
                Toast.makeText(this, R.string.err_internet_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Checks whether the user is currently at the last page
     * @return True if the user is at the last page
     */
    private boolean isAtLastPage() {
        return mPager.getCurrentItem() == OtherUntowardPagerAdapter.NUM_PAGES - 1;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageScrollStateChanged(int state) { }

    @Override
    public void onPageSelected(int position) {
        if (isAtLastPage()) {
            // Change the next button to Finish/Send Button if at last page
            mNextButton.setText(getString(R.string.send));
            mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.send_btn_color_state_list));
        } else {
            mNextButton.setText(getString(R.string.next));
            mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UploadFileFragment.sPathList = null;
        UploadFileFragment.sFileNameList = null;
        PatientReportDescriptionFragment.sReportDescription = null;

        // Delete any cached image files
        boolean isSuccess = Utils.deleteAllPrivateFiles(this);
        // TODO: Remove these messages on release
//        if (isSuccess) {
//            Toast.makeText(this, "File Deletion Success", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "File Deletion Failure", Toast.LENGTH_SHORT).show();
//        }
        System.gc();
    }
}