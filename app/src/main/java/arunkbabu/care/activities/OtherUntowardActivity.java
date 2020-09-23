package arunkbabu.care.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arunkbabu.care.Constants;
import arunkbabu.care.ExtensionsKt;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.adapters.OtherUntowardPagerAdapter;
import arunkbabu.care.fragments.PatientReportDescriptionFragment;
import arunkbabu.care.fragments.ReportProblemFragment;
import arunkbabu.care.fragments.UploadFileFragment;
import arunkbabu.care.views.CircularImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OtherUntowardActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    @BindView(R.id.tv_otheruntoward_error) TextView mErrorTextView;
    @BindView(R.id.tv_otheruntoward_doctor_name) TextView mDocNameTextView;
    @BindView(R.id.iv_otheruntoward_doctor_dp) CircularImageView mDocDpImageView;
    @BindView(R.id.vp_otheruntoward) ViewPager mPager;
    @BindView(R.id.btn_otheruntoward_next) MaterialButton mNextButton;
    @BindView(R.id.tv_otheruntoward_progress) TextView mProgressValue;
    @BindView(R.id.pb_otheruntoward) CircularProgressBar mProgressBar;
    @BindView(R.id.pb_otheruntoward_dp) ProgressBar mDpProgressBar;
    @BindView(R.id.otheruntoward_reporting_doctor) TextView mReportingDoctorTextView;

    private OtherUntowardPagerAdapter mAdapter;
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseUser mUser;

    private String mDocName;
    private String mDocDpPath;
    private String mPatientDpPath;
    private String mPatientName;

    private int mFileCount = 0;
    private ArrayList<Uri> mDownloadURIs;
    private boolean mIsNetworkConnected;
    private boolean mIsAccountAlreadyVerified;

    public static String sReportingDoctorId;
    public static boolean sIsNewDoctorSelected = false;
    public static boolean isOtherUntowardActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_untoward);
        ButterKnife.bind(this);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
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

        // Get patient data from intent
        sReportingDoctorId = getIntent().getStringExtra(ReportProblemFragment.REPORTING_DOCTOR_ID_EXTRAS_KEY);
        mPatientName = getIntent().getStringExtra(ReportProblemFragment.PATIENT_NAME_EXTRAS_KEY);
        mPatientDpPath = getIntent().getStringExtra(ReportProblemFragment.PATIENT_DP_EXTRAS_KEY);
        mDocDpPath = getIntent().getStringExtra(ReportProblemFragment.DOCTOR_DP_EXTRAS_KEY);
        mDocName = getIntent().getStringExtra(ReportProblemFragment.DOCTOR_NAME_EXTRAS_KEY);

        if (sReportingDoctorId == null)
            sReportingDoctorId = "";

        if (mPatientName == null)
            mPatientName = "";

        if (mPatientDpPath == null)
            mPatientDpPath = "";

        if (mDocName == null)
            mDocName = "";

        if (mDocDpPath == null)
            mDocDpPath = "";

        if (sReportingDoctorId.equals(""))
            Utils.showErrorDialog(this, getString(R.string.info_select_doctor), "", getString(R.string.close));

        if (mDocName.equals(""))
            fetchDoctorDetails(false);
        else
            loadViews(false);

        mDocNameTextView.setOnClickListener(this);
        mDocDpImageView.setOnClickListener(this);

        isOtherUntowardActivityActive = true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_otheruntoward_doctor_name:
                startActivity(new Intent(this, SearchDoctorActivity.class));
                break;
            case R.id.iv_otheruntoward_doctor_dp:
                Intent dpIntent = new Intent(this, ViewPictureActivity.class);
                dpIntent.putExtra(ViewPictureActivity.PROFILE_PICTURE_PATH_EXTRA_KEY, mDocDpPath);
                startActivity(dpIntent);
                break;
        }
    }

    /**
     * Fetches the name, dp of the doctor from database
     * Includes Doctor Name: sDocName
     * Doctor DP: sDocDpPath
     * @param reload Clears all the data in fragments
     */
    private void fetchDoctorDetails(boolean reload) {
        if (!sReportingDoctorId.isEmpty()) {
            mDb.collection(Constants.COLLECTION_USERS).document(sReportingDoctorId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot s = task.getResult();
                            if (s != null) {
                                // Fetch success
                                mDocName = s.getString(Constants.FIELD_FULL_NAME);
                                mDocDpPath = s.getString(Constants.FIELD_PROFILE_PICTURE);
                                mDocNameTextView.setText((mDocName == null || mDocName.equals("")) ? getString(R.string.not_set) : mDocName);

                                // Hide the Loading message & Show the Constipation layout
                                loadViews(reload);
                            } else {
                                Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.err_unable_to_fetch), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            mDocNameTextView.setText(R.string.not_set);
            loadViews(reload);
        }
    }

    /**
     * Upload the image files selected
     */
    private void uploadImageFiles() throws IOException {
        mNextButton.setEnabled(false);
        mProgressBar.setIndeterminateMode(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressValue.setVisibility(View.VISIBLE);

        if (mUser != null) {
            Toast.makeText(this, getString(R.string.uploading_files), Toast.LENGTH_SHORT).show();
            // Upload the image files
            ArrayList<Uri> imgPaths = UploadFileFragment.sPathList;
            ArrayList<String> fileNames = UploadFileFragment.sFileNameList;

            mFileCount = imgPaths.size();
            mProgressValue.setText(String.valueOf(mFileCount));
            for (int i = 0; i < imgPaths.size(); i++) {
                // Upload all file one by one
                String uploadPath = mUser.getUid() + "/" + Constants.DIRECTORY_SENT_IMAGES + fileNames.get(i);

                Uri uri = imgPaths.get(i);
                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }
                Bitmap rBitmap = ExtensionsKt.resize(bitmap, Constants.IMG_UPLOAD_SIZE, Constants.IMG_UPLOAD_SIZE);

                // Compress and convert the image bitmap to InputStream
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                rBitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPG_QUALITY, bos);
                ByteArrayInputStream bs = new ByteArrayInputStream(bos.toByteArray());

                StorageReference storageReference = mStorage.getReference(uploadPath);
                UploadTask uploadTask = storageReference.putStream(bs);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // Upload failed
                        Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show();
                        return null;
                    }
                    return storageReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mProgressBar.setIndeterminateMode(true);
                        mFileCount--;
                        mProgressValue.setText(String.valueOf(mFileCount));

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
                uploadTask.addOnProgressListener(snapshot -> {
                    float progress = (float) (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if ((int) progress > 4) {
                        mProgressBar.setIndeterminateMode(false);
                    }
                    mProgressBar.setProgressWithAnimation(progress);
                });
            }
        }
    }

    /**
     * Pushes the report to database
     */
    private void createReport() {
        mProgressBar.setIndeterminateMode(true);
        mProgressValue.setVisibility(View.GONE);
        if (mUser != null) {
            Toast.makeText(this, getString(R.string.creating_report), Toast.LENGTH_SHORT).show();
            String problemReportPath = Constants.COLLECTION_USERS + "/" + mUser.getUid() + "/" + Constants.COLLECTION_PROBLEM_REPORT;

            Map<String, Object> downloadUrls = new HashMap<>();
            for (int i = 0; i < mDownloadURIs.size(); i++) {
                // Stringify the URI for uploading to db; Otherwise it won't work
                String url = (mDownloadURIs.get(i) != null) ? mDownloadURIs.get(i).toString() : "";
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
     *
     * @param reportId The String id of the report
     */
    private void sendRequestToDoctor(String reportId) {
        String docRequestListPath = Constants.COLLECTION_USERS + "/" + sReportingDoctorId + "/" + Constants.COLLECTION_PATIENT_REQUEST;
        if (mUser != null) {
            // Get the user's id
            String userId = mUser.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put(Constants.FIELD_PATIENT_ID, userId);
            data.put(Constants.FIELD_IS_A_VALID_REQUEST, true);
            data.put(Constants.FIELD_PATIENT_NAME, mPatientName);
            data.put(Constants.FIELD_PROFILE_PICTURE, mPatientDpPath);
            data.put(Constants.FIELD_REPORT_TYPE, Constants.REPORT_TYPE_OTHER);
            data.put(Constants.FIELD_REPORT_ID, reportId);
            data.put(Constants.FIELD_REQUEST_TIMESTAMP, Timestamp.now());

            mDb.collection(docRequestListPath).add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this,
                                getString(R.string.request_sent, mDocName), Toast.LENGTH_LONG).show();
                        mNextButton.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                getString(R.string.err_report_creation_fail), Toast.LENGTH_SHORT).show();
                        mNextButton.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    });
        } else {
            Toast.makeText(this, getString(R.string.err_user_not_logged_in), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    /**
     * Make the views visible & hide the ErrorTextView
     * @param reload True Clears all the fragments data
     */
    private void loadViews(boolean reload) {
        if (mDocDpPath != null)
            Utils.loadDpToView(this, mDocDpPath, mDocDpImageView);

        mDocNameTextView.setText(mDocName.equals("") ? getString(R.string.not_set) : mDocName);

        if (!reload) {
            mAdapter = new OtherUntowardPagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mAdapter);
            mPager.addOnPageChangeListener(this);
        }

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
     *
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
            if (PatientReportDescriptionFragment.sReportDescription == null || PatientReportDescriptionFragment.sReportDescription.equals("")) {
                Toast.makeText(this, R.string.err_empty_report, Toast.LENGTH_LONG).show();
                return;
            } else if (sReportingDoctorId == null || sReportingDoctorId.isEmpty()) {
                Toast.makeText(this, R.string.err_no_doc_selected, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsNetworkConnected) {
                // Internet Available
                if (mIsAccountAlreadyVerified) {
                    // Only SEND if account is VERIFIED
                    if (UploadFileFragment.sPathList != null && !UploadFileFragment.sPathList.isEmpty()) {
                        // Only upload the images if they are present or selected for upload
                        // In other-words, if there are no images don't upload it
                        try {
                            uploadImageFiles();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, getString(R.string.err_get_download_image_url), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // No files to upload; so start creating report immediately
                        createReport();
                        mNextButton.setEnabled(false);
                        mProgressBar.setIndeterminateMode(true);
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
     *
     * @return True if the user is at the last page
     */
    private boolean isAtLastPage() {
        return mPager.getCurrentItem() == OtherUntowardPagerAdapter.NUM_PAGES - 1;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageSelected(int position) {
        if (isAtLastPage()) {
            // Change the next button to Finish/Send Button if at last page
            mNextButton.setText(getString(R.string.send));
            mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.send_btn_color_state_list));
            mNextButton.setTextColor(ContextCompat.getColor(this, R.color.colorAcceptButton));
            mNextButton.setStrokeColorResource(R.color.colorAcceptButton);
        } else {
            mNextButton.setText(getString(R.string.next));
            mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list));
            mNextButton.setTextColor(ContextCompat.getColor(this, R.color.colorLightIndigoNormal));
            mNextButton.setStrokeColorResource(R.color.colorLightIndigoNormal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sIsNewDoctorSelected) {
            fetchDoctorDetails(true);
            PatientActivity.isNewDoctorSelected = true;
            sIsNewDoctorSelected = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOtherUntowardActivityActive = false;

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