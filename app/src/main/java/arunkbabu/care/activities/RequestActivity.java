package arunkbabu.care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import arunkbabu.care.Constants;
import arunkbabu.care.Patient;
import arunkbabu.care.R;
import arunkbabu.care.adapters.RequestListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestActivity extends AppCompatActivity implements RequestListAdapter.ItemClickListener,
        OnCompleteListener<QuerySnapshot>, FirebaseAuth.AuthStateListener {
    @BindView(R.id.rv_request_view) RecyclerView mRequestRecyclerView;
    @BindView(R.id.tv_request) TextView mRequestTextView;
    @BindView(R.id.tv_no_requests) TextView mNoRequestTextView;

    private RequestListAdapter mAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private String mRequestsCollectionPath;
    private Query mGetRequestsQuery;
    private boolean mIsLaunched;

    private final String TAG = RequestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        ButterKnife.bind(this);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorDarkBackgroundGrey2));

        mRequestRecyclerView.setHasFixedSize(false);
        mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get Request details from database
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mRequestsCollectionPath = Constants.COLLECTION_USERS + "/"
                    + user.getUid() + "/" + Constants.COLLECTION_PATIENT_REQUEST;

            mGetRequestsQuery = mDb.collection(mRequestsCollectionPath)
                    .whereEqualTo(Constants.FIELD_IS_A_VALID_REQUEST, true)
                    .orderBy(Constants.FIELD_REQUEST_TIMESTAMP, Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<Patient> options = new FirestoreRecyclerOptions.Builder<Patient>()
                    .setLifecycleOwner(this)
                    .setQuery(mGetRequestsQuery, snapshot -> {
                        // Get the id of the document and put it in the Patient POJO
                        Patient p = snapshot.toObject(Patient.class);
                        if (p != null) {
                            p.setDocumentId(snapshot.getId());
                            return p;
                        }
                        return null;
                    })
                    .build();

            LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mAdapter = new RequestListAdapter(options, mNoRequestTextView, mRequestRecyclerView);
            mAdapter.setClickListener(this);
            mRequestRecyclerView.setLayoutManager(lm);
            mRequestRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onItemClick(View v, Patient patient, int position) {
        switch (v.getId()) {
            case R.id.btn_accept:
                onAcceptButtonClick(patient, position);
                break;
        }
    }

    /**
     * Accept Button click is handled here
     * @param patient Patient object which contains the patient request details
     * @param position The position of the view clicked.
     */
    private void onAcceptButtonClick(Patient patient, int position) {
        // Get the basic patient request details
        String patientId = patient.getPatientId();
        String reportId = patient.getReportId();
        String requestId = patient.getDocumentId();
        int reportType = patient.getReportType();

        Intent docIntent = new Intent(this, DoctorActivity.class);
        docIntent.putExtra(Constants.PATIENT_ID_KEY, patientId);
        docIntent.putExtra(Constants.PATIENT_REPORT_ID_KEY, reportId);
        docIntent.putExtra(Constants.PATIENT_REPORT_TYPE_KEY, reportType);
        docIntent.putExtra(Constants.PATIENT_REQUEST_ID_KEY, requestId);

        startActivity(docIntent);
    }

    /**
     * Database query result will be available here
     * @param task Contains the database query result
     */
    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGetRequestsQuery.addSnapshotListener(this, (queryDocumentSnapshots, e) -> mAdapter.notifyDataSetChanged());
        mAuth.addAuthStateListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        // User is either signed out or the login credentials no longer exists. So launch the login
        // activity again for the user to sign-in
        if (firebaseAuth.getCurrentUser() == null && !mIsLaunched) {
            mIsLaunched = true;
            startActivity(new Intent(this, LoginActivity.class));
            Toast.makeText(this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}