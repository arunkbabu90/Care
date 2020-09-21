package arunkbabu.care.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.fragments.DoctorsReportsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewDoctorReportActivity extends AppCompatActivity {
    @BindView(R.id.tv_viewDocReport_docName) TextView mDocNameTextView;
    @BindView(R.id.tv_viewDocReport_error) TextView mErrorTextView;
    @BindView(R.id.tv_viewDocReport_speciality) TextView mSpecialityQualificationTextView;
    @BindView(R.id.tv_viewDocReport_hospital) TextView mHospitalTextView;
    @BindView(R.id.tv_viewDocReport_medicines) TextView mMedicinesTextView;
    @BindView(R.id.tv_viewDocReport_instructions) TextView mInstructionsTextView;
    @BindView(R.id.tv_viewDocReport_ur_desc) TextView mYourDescriptionTextView;
    @BindView(R.id.tv_viewDocReport_date) TextView mDateTextView;
    @BindView(R.id.pb_view_doctor_report) ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private String mDocName;
    private String mInstructions = "";
    private String mYourDescription = "";
    private String mDocSpecialityQualifications = "";
    private String mDocHospitalName = "";
    private ArrayList<String> mMedicines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup shared element transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkBackgroundGrey1));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorDarkBackgroundGrey1));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_doctor_report);
        ButterKnife.bind(this);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String reportId = getIntent().getStringExtra(DoctorsReportsFragment.KEY_EXTRA_REPORT_ID);
        String reportReceivedDate = getIntent().getStringExtra(DoctorsReportsFragment.KEY_EXTRA_REPORT_TIMESTAMP);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && reportId != null) {
            String docReportDetailsPath = Constants.COLLECTION_REPORT_DETAILS + "/"
                    + user.getUid() + "/" + Constants.COLLECTION_DOCTOR_REPORT;

            mDb.collection(docReportDetailsPath).document(reportId).get()
                    .addOnCompleteListener(task -> {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            mErrorTextView.setVisibility(View.GONE);

                            DocumentSnapshot d = task.getResult();
                            if (d != null && d.exists()) {
                                // Fetch success so populate the report
                                mDocName = d.getString(Constants.FIELD_FULL_NAME);
                                mInstructions = d.getString(Constants.FIELD_DOCTOR_MEDICATION_INSTRUCTIONS);
                                mYourDescription = d.getString(Constants.FIELD_PROBLEM_DESCRIPTION);
                                mDocHospitalName = d.getString(Constants.FIELD_WORKING_HOSPITAL_NAME);
                                mDocSpecialityQualifications = d.getString(Constants.FIELD_SPECIALITY_QUALIFICATIONS);

                                if (mInstructions == null)
                                    mInstructions = "";

                                if (mYourDescription == null)
                                    mYourDescription = "";

                                if (mDocHospitalName == null)
                                    mDocHospitalName = "";

                                if (mDocSpecialityQualifications == null)
                                    mDocSpecialityQualifications = "";

                                HashMap<String, Object> meds = (HashMap<String, Object>) d.get(Constants.FIELD_DOCTOR_MEDICINES);
                                if (meds != null) {
                                    for (int i = 0; i < meds.size(); i++) {
                                        mMedicines.add((String) meds.get("med" + i));
                                    }
                                }
                            } else {
                                mErrorTextView.setVisibility(View.VISIBLE);
                                mErrorTextView.setText(getText(R.string.err_unable_to_fetch));
                            }

                            // Assign the data to the respective text views
                            StringBuilder sb = new StringBuilder();
                            if (mMedicines != null) {
                                for (int i = 0; i < mMedicines.size(); i++) {
                                    sb.append("• ");
                                    sb.append(mMedicines.get(i));
                                    sb.append("\n");
                                }
                            } else {
                                sb.append("");
                            }

                            mDocNameTextView.setText(getString(R.string.doc_name, mDocName));
                            mSpecialityQualificationTextView.setText(mDocSpecialityQualifications);

                            if (mDocHospitalName.equals(""))
                                mHospitalTextView.setVisibility(View.GONE);
                            else
                                mHospitalTextView.setText(mDocHospitalName);

                            mMedicinesTextView.setText(sb.toString());
                            mInstructionsTextView.setText(mInstructions);
                            mYourDescriptionTextView.setText(mYourDescription);
                            mDateTextView.setText(reportReceivedDate);
                        } else {
                            mErrorTextView.setVisibility(View.VISIBLE);
                            mErrorTextView.setText(getText(R.string.err_unable_to_fetch));
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failed to fetch report details
                        mProgressBar.setVisibility(View.GONE);
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mErrorTextView.setText(getText(R.string.err_unable_to_fetch));
                    });
        }
    }
}