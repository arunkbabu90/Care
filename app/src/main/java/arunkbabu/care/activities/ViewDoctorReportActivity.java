package arunkbabu.care.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.fragments.DoctorsReportsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewDoctorReportActivity extends AppCompatActivity {
    @BindView(R.id.tv_view_doctor_report) TextView mViewReportTextView;
    @BindView(R.id.pb_view_doctor_report) ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private String mDocName;
    private String mReportType;
    private String mDescription;
    private ArrayList<String> mMedicines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_doctor_report);
        ButterKnife.bind(this);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mMedicines = new ArrayList<>();

        String reportId = getIntent().getStringExtra(DoctorsReportsFragment.KEY_EXTRA_REPORT_ID);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && reportId != null) {
            String docReportDetailsPath = Constants.COLLECTION_REPORT_DETAILS + "/"
                    + user.getUid() + "/" + Constants.COLLECTION_DOCTOR_REPORT;

            mDb.collection(docReportDetailsPath).document(reportId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.GONE);
                            DocumentSnapshot d = task.getResult();
                            if (d != null && d.exists()) {
                                // Fetch success so populate the report
                                mDocName = d.getString(Constants.FIELD_FULL_NAME);
                                mReportType = Utils.toReportTypeString(d.getLong(Constants.FIELD_REPORT_TYPE).intValue());
                                mDescription = d.getString(Constants.FIELD_DOCTOR_MEDICATION_INSTRUCTIONS);

                                HashMap<String, Object> meds = (HashMap<String, Object>) d.get(Constants.FIELD_DOCTOR_MEDICINES);
                                if (meds != null) {
                                    for (int i = 0; i < meds.size(); i++) {
                                        mMedicines.add((String) meds.get("med" + i));
                                    }
                                }
                            }

                            // Build a string to display medicines in the text view; as we are using only a single text view for now
                            StringBuilder sb = new StringBuilder();
                            sb.append("Report ID: ");
                            sb.append(reportId);
                            sb.append("\n\nDoctor Name: ");
                            sb.append(mDocName);
                            sb.append("\n\nReport Type: ");
                            sb.append(mReportType);
                            sb.append("\n\nDescription:\n");
                            sb.append(mDescription);
                            sb.append("\n\nMedicines:\n");
                            if (mMedicines != null) {
                                for (int i = 0; i < mMedicines.size(); i++) {
                                    sb.append("\t\t• ");
                                    sb.append(mMedicines.get(i));
                                    sb.append("\n");
                                }
                            } else {
                                sb.append("");
                            }

                            mViewReportTextView.setText(sb.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failed to fetch report details
                        mViewReportTextView.setText(getText(R.string.err_unable_to_fetch));
                    });
        }
    }
}