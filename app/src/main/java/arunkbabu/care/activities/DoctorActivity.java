package arunkbabu.care.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.adapters.DocOtherUntowardPagerAdapter;
import arunkbabu.care.fragments.DocAddMedicineFragment;
import arunkbabu.care.fragments.DocInstructionFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DoctorActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.vp_doc_pain) ViewPager mDocPager;
    @BindView(R.id.btn_doc_next) MaterialButton mNextButton;
    @BindView(R.id.tv_doc_error_text) TextView mErrorTextView;
    @BindView(R.id.pb_doc_next) ProgressBar mProgressCircle;

    private static int sReportType;
    private static String sPatientId;
    private static String sReportId;
    private static String sRequestId;
    private static String sPatientName;
    private static String sPatientAge;
    private static String sPastMedications;
    private static Long sPainIntensity;
    private static Boolean sIsFullBody;
    private static String sReportDescription;
    private static String sQuestionnaire;
    private static int sPatientSex;
    private ArrayList<String> mImagePaths;

    public static String sBodyFrontURL;
    public static String sBodyBackURL;
    public static String sBodySideURL;

    private Long mConstipationIntensity;
    private Long mConstipationDuration;
    private String mSymptoms;

    private Long mDiarrheaDuration;
    private Long mDiarrheaPoopType;
    private Long mDiarrheaPoopColor;
    private String mPoopConditions;

    private Long mFeverDuration;
    private ArrayList<Integer> mFeverCelsiusTemperatures;
    private ArrayList<Integer> mFeverFahrenheitTemperatures;

    private Timestamp mReportTimestamp;

    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;

    private final String TAG = "DoctorActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        ButterKnife.bind(this);

        // Set flag as doctor
        Utils.userType = Constants.USER_TYPE_DOCTOR;

        mImagePaths = new ArrayList<>();
        mFeverCelsiusTemperatures = new ArrayList<>();
        mFeverFahrenheitTemperatures = new ArrayList<>();

        // Get the patient id and report type from intent
        sPatientId = getIntent().getStringExtra(Constants.PATIENT_ID_KEY);
        sReportType = getIntent().getIntExtra(Constants.PATIENT_REPORT_TYPE_KEY, Constants.NULL_INT);
        sReportId = getIntent().getStringExtra(Constants.PATIENT_REPORT_ID_KEY);
        sRequestId = getIntent().getStringExtra(Constants.PATIENT_REQUEST_ID_KEY);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // TODO: Remove this line :LOG
        Log.i(TAG, "onCreate: Loaded patient with id: " + sPatientId + " and Report Type: " + Utils.toReportTypeString(sReportType));

        // Load the entire data of the patient
        fetchPatientData();

        mNextButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Retrieves all patient's report data and patients profile data from the database
     */
    private void fetchPatientData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDb.collection(Constants.COLLECTION_USERS).document(sPatientId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot d = task.getResult();
                            if (d != null) {
                                // Fetch success
                                sPatientName = d.getString(Constants.FIELD_FULL_NAME);
                                Long age = d.getLong(Constants.FIELD_DOB);
                                if (age != null) {
                                    Calendar c = Calendar.getInstance();
                                    c.setTimeInMillis(age);

                                    sPatientAge = String.valueOf(Utils.calculateAge(c.get(Calendar.DAY_OF_MONTH),
                                            c.get(Calendar.MONTH), c.get(Calendar.YEAR)));
                                } else {
                                    sPatientAge = getString(R.string.not_provided);
                                }

                                Long sex = d.getLong(Constants.FIELD_SEX);
                                if (sex != null) {
                                    sPatientSex = sex.intValue();
                                }
                                // Set a default value if no sex is provided
                                if (sPatientSex != Constants.SEX_FEMALE && sPatientSex != Constants.SEX_MALE) {
                                    sPatientSex = Constants.NULL_INT;
                                }

                                // Get the pain report details
                                String problemReportPath = Constants.COLLECTION_USERS + "/" + sPatientId
                                        + "/" + Constants.COLLECTION_PROBLEM_REPORT;
                                fetchReportDetails(problemReportPath);
                            } else {
                                Toast.makeText(this,
                                        getString(R.string.err_unable_to_fetch),
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this,
                                    getString(R.string.err_unable_to_fetch),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    /**
     * Retrieve the details of the patient's report
     * @param problemReportPath The path containing the patient's report
     */
    private void fetchReportDetails(String problemReportPath) {
        switch (sReportType) {
            case Constants.REPORT_TYPE_OTHER: {
                mDb.collection(problemReportPath).document(sReportId).get()
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot d = task.getResult();
                                if (d != null) {
                                    // Report Fetch Success
                                    sReportDescription = d.getString(Constants.FIELD_PROBLEM_DESCRIPTION);
                                    mReportTimestamp = d.getTimestamp(Constants.FIELD_REPORT_TIMESTAMP);

                                    // Retrieve all the paths of images uploaded
                                    HashMap<String, Object> b = (HashMap<String, Object>) d.get(Constants.FIELD_IMAGE_UPLOADS);
                                    if (b != null) {
                                        for (int i = 0; i < b.size(); i++) {
                                            mImagePaths.add((String) b.get("img" + i));
                                        }
                                    }

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
                break;
            }
        }
    }

    /**
     * Load the appropriate view based on the report type; Make the views visible and hide the ErrorTextView
     */
    private void loadViews() {
        switch (sReportType) {
            case Constants.REPORT_TYPE_OTHER: {
                DocOtherUntowardPagerAdapter otherAdapter = new DocOtherUntowardPagerAdapter(
                        getSupportFragmentManager(), this, this);
                mDocPager.setAdapter(otherAdapter);
                mDocPager.setOffscreenPageLimit(3);
                break;
            }
        }

        mDocPager.addOnPageChangeListener(this);
        mDocPager.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
    }

    public ArrayList<String> getUploadedImagePaths() {
        return mImagePaths;
    }

    public static String getPatientName() {
        return sPatientName;
    }

    public static String getPatientAge() {
        return sPatientAge;
    }

    public static String getPastMedications() {
        return sPastMedications;
    }

    public static String getReportDescription() {
        // Provide a default string if not data is available
        return (sReportDescription == null || sReportDescription.equals("")) ? "Not Provided" : sReportDescription;
    }

    /**
     * Define Next Button Click behaviour of the Doctor
     * @param view The current button view
     */
    public void onDocNextPress(View view) {
        // Hide virtual keyboard
        Utils.closeSoftInput(this);

        if (!isAtLastPage()) {
            mDocPager.setCurrentItem(mDocPager.getCurrentItem() + 1, true);
        } else {
            // Send the report to patient
            createReport();
        }
    }

    private void createReport() {
        Toast.makeText(this, getString(R.string.creating_report), Toast.LENGTH_SHORT).show();
        mNextButton.setEnabled(false);
        mProgressCircle.setVisibility(View.VISIBLE);

        String docReportPath = Constants.COLLECTION_REPORT_DETAILS + "/"
                + sPatientId + "/" + Constants.COLLECTION_DOCTOR_REPORT;

        ArrayList<String> medList = DocAddMedicineFragment.mMedicineList;
        Map<String, Object> medicines = new HashMap<>();
        for (int i = 0; i < medList.size(); i++) {
            medicines.put("med" + i, medList.get(i));
        }

        Map<String, Object> prescription = new HashMap<>();
        prescription.put(Constants.FIELD_DOCTOR_NAME, "Joe Doe"); // TODO: Get the real name of the doctor from database
        prescription.put(Constants.FIELD_REPORT_TYPE, sReportType);
        prescription.put(Constants.FIELD_DOCTOR_MEDICATION_INSTRUCTIONS, DocInstructionFragment.sDocReportDescription);
        prescription.put(Constants.FIELD_DOCTOR_MEDICINES, medicines);
        // Put the time when the patient has created the report to the doctor
        // This helps the patient to identify the report when doctor sends the prescription for the patient
        prescription.put(Constants.FIELD_REPORT_TIMESTAMP, mReportTimestamp);

        mDb.collection(docReportPath).add(prescription)
                .addOnSuccessListener(documentReference -> {
                    // Report Upload Success
                    sentReportToPatient(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Report Upload Failure
                    Toast.makeText(this, getString(R.string.err_report_create_fail), Toast.LENGTH_SHORT).show();
                    mNextButton.setEnabled(true);
                    mProgressCircle.setVisibility(View.GONE);
                });
    }

    private void sentReportToPatient(String reportId) {
        String docShortReportPath = Constants.COLLECTION_USERS + "/" + sPatientId + "/" + Constants.COLLECTION_DOCTOR_REPORT;

        Map<String, Object> shortReport = new HashMap<>();
        shortReport.put(Constants.FIELD_DOCTOR_NAME, "Joe Doe"); // TODO: Get the real name of the doctor from database
        shortReport.put(Constants.FIELD_REPORT_TYPE, sReportType);
        shortReport.put(Constants.FIELD_IS_A_VALID_DOCTOR_REPORT, true);
        shortReport.put(Constants.FIELD_REQUEST_TIMESTAMP, Timestamp.now());
        shortReport.put(Constants.FIELD_REPORT_ID, reportId);
        // Put the time when the patient has created the report to the doctor
        // This helps the patient to identify the report when doctor sends the prescription for the patient
        shortReport.put(Constants.FIELD_REPORT_TIMESTAMP, mReportTimestamp);

        mDb.collection(docShortReportPath).add(shortReport)
                .addOnSuccessListener(documentReference -> {
                    // Report Upload Success: So delete the request
                    deleteRequest();
                })
                .addOnFailureListener(e -> {
                    // Report Upload Failure
                    Toast.makeText(this, getString(R.string.err_report_create_fail), Toast.LENGTH_SHORT).show();
                    mNextButton.setEnabled(true);
                    mProgressCircle.setVisibility(View.GONE);
                });
    }

    /**
     * Deletes the patient's request from the doctor's request list
     */
    private void deleteRequest() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String docRequestListPath = Constants.COLLECTION_USERS + "/"
                    + user.getUid() + "/" + Constants.COLLECTION_PATIENT_REQUEST;

            mDb.collection(docRequestListPath).document(sRequestId).delete()
                    .addOnCompleteListener(task -> {
                        // Request Deletion Success
                        Toast.makeText(DoctorActivity.this, getString(R.string.report_sent, sPatientName), Toast.LENGTH_SHORT).show();
                        mNextButton.setEnabled(true);
                        mProgressCircle.setVisibility(View.GONE);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // On failure try again
                        Toast.makeText(this, R.string.err_request_deletion_fail, Toast.LENGTH_LONG).show();
                        deleteRequest();
                    });
        }
    }

    /**
     * Checks whether the user is currently at the last page
     * @return True if the user is at the last page
     */
    private boolean isAtLastPage() {
        switch (sReportType) {
            case Constants.REPORT_TYPE_OTHER:
                return mDocPager.getCurrentItem() == DocOtherUntowardPagerAdapter.NUM_PAGES - 1;
            default:
                return true;
        }
    }

    // This method will be invoked when a new page becomes selected.
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    // This method will be invoked when the current page is scrolled
    @Override
    public void onPageSelected(int position) {
        switchButtonStates(position);
    }

    // Called when the scroll state changes:
    // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
    @Override
    public void onPageScrollStateChanged(int state) { }

    private void switchButtonStates(int position) {
        // Change the button appearance and behaviour based on the pages
        switch (sReportType) {
            case Constants.REPORT_TYPE_OTHER: {
                switch (position) {
                    case 2:
                        mNextButton.setText(getString(R.string.add_medicine));
                        mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list));
                        break;
                    case 3:
                        mNextButton.setText(getString(R.string.add_a_note));
                        mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list));
                        break;
                    case 4:
                        mNextButton.setText(getString(R.string.send));
                        mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.send_btn_color_state_list));
                        break;
                    default:
                        mNextButton.setText(getString(R.string.next));
                        mNextButton.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.next_btn_color_state_list));
                }
                break;
            }
        }
    }
}