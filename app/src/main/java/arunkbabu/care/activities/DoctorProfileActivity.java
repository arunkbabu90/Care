package arunkbabu.care.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.dialogs.DatePickerDialog;
import arunkbabu.care.views.CircularImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DoctorProfileActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.DateChangeListener, TextWatcher {
    @BindView(R.id.doc_profile_loading_layout) FrameLayout mLoadingLayout;
    @BindView(R.id.tv_doc_profile_name) TextView mHiTextView;
    @BindView(R.id.iv_doc_profile_dp) CircularImageView mDpView;
    @BindView(R.id.btn_doc_profile_sign_out) MaterialButton mSignOutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private boolean mIsDataJustLoadedFromDatabase, mIsDataLoading, mIsDataLoadFailed, mIsUpdatesAvailable;
    private int mSpeciality = Constants.SPECIALITY_GENERAL;
    private int mUserType;
    private String mFirstName, mDob;
    private long mEpochDob = Constants.NULL_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        ButterKnife.bind(this);

        // Get instances
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        // Tint Status & navigation bar

        // Populate the spinner with days
        /*
        ArrayAdapter<CharSequence> mSpecialityAdapter = ArrayAdapter.createFromResource(this, R.array.specialities, R.layout.item_spinner_text_centred);
        mSpecialityAdapter.setDropDownViewResource(R.layout.item_spinner_drop_down_centered);
        mSpecialitySpinner.setAdapter(mSpecialityAdapter);
        mSpecialitySpinner.setOnItemSelectedListener(this);
        */

        // Get data from database if available
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mIsDataLoading = true;
            String uid = user.getUid();
            mDb.collection(Constants.COLLECTION_USERS).document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot d = task.getResult();
                        if (d != null) {
                            // Document Successfully fetched
                            Long userType = d.getLong(Constants.FIELD_USER_TYPE);
                            if (userType != null) {
                                mUserType = userType.intValue();
                                Utils.userType = mUserType;
                            }

                            mFirstName = d.getString(Constants.FIELD_FULL_NAME);

                            /* Fetch and populate the data to views */

                            // Set Hi Dr. ........ text
                            String docName = mFirstName;
                            mHiTextView.setText(getString(R.string.doc_hi_text, docName));

                            // Set speciality
                            Long speciality = d.getLong(Constants.FIELD_DOCTOR_SPECIALITY);
                            if (speciality != null) {
                                mSpeciality = speciality.intValue();
//                                mSpecialitySpinner.setSelection(mSpeciality);
                                if (mSpeciality == Constants.SPECIALITY_OTHER) {
                                    // Other Speciality: So fetch the Other Speciality also
//                                    mOtherSpecialityField.setText(d.getString(Constants.FIELD_DOCTOR_OTHER_SPECIALITY));
                                }
                            }

                            // Set Date of Birth
                            Long dob = d.getLong(Constants.FIELD_DOB);
                            if (dob != null) {
                                mEpochDob = dob;
                                mDob = Utils.convertEpochToDateString(dob);

                                Calendar c = Calendar.getInstance();
                                c.setTimeInMillis(dob);

//                                mDobTextView.setText(mDob);
                            } else {
                                mEpochDob = Constants.NULL_INT;
                            }

                            // Set Experience
                            Long exp = d.getLong(Constants.FIELD_DOCTOR_EXPERIENCE);
                            if (exp != null) {
//                                mExperienceField.setText(String.valueOf(exp.intValue()));
                            }

                            // Set Registration No
                            Long reg = d.getLong(Constants.FIELD_REGISTRATION_NO);
                            if (reg != null) {
//                                mRegistrationNoField.setText(String.valueOf(reg.longValue()));
                            }

                            // Set Hospital Name
//                            mHospitalField.setText(d.getString(Constants.FIELD_HOSPITAL_NAME));
                        }
                        mIsDataLoading = false;
                        mIsDataLoadFailed = false;
                        mIsDataJustLoadedFromDatabase = true;
                        mLoadingLayout.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, R.string.err_unable_to_fetch, Toast.LENGTH_SHORT).show();
                        mIsDataLoading = false;
                        mIsDataLoadFailed = true;
                        mLoadingLayout.setVisibility(View.GONE);
                    });
        }

//        mRegistrationNoField.addTextChangedListener(this);
//        mDobTextView.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
    }

    /**
     * Pushes all the data to the database
     */
    private void pushToDatabase() {
        // Update profile in the database
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> profileData = new HashMap<>();

            profileData.put(Constants.FIELD_USER_TYPE, mUserType);

//            // Speciality
//            if (mOtherSpecialityField.getText() != null && mSpeciality == Constants.SPECIALITY_OTHER) {
//                // Other speciality: So get the user typed speciality from the EditText
//                String speciality = mOtherSpecialityField.getText().toString();
//                if (!speciality.equals("")) {
//                    // Save it only if it's NOT empty
//                    profileData.put(Constants.FIELD_DOCTOR_SPECIALITY, mSpeciality);
//                    profileData.put(Constants.FIELD_DOCTOR_OTHER_SPECIALITY, speciality);
//                }
//            } else if (mSpeciality != Constants.NULL_INT) {
//                // Some other pre-defined speciality: If its not empty save it
//                profileData.put(Constants.FIELD_DOCTOR_SPECIALITY, mSpeciality);
//            }

            // Date of birth
            if (mEpochDob != Constants.NULL_INT)
                profileData.put(Constants.FIELD_DOB, mEpochDob);


            if (mFirstName != null && !mFirstName.equals(""))
                profileData.put(Constants.FIELD_FULL_NAME, mFirstName);

            mDb.collection(Constants.COLLECTION_USERS).document(user.getUid())
                    .update(profileData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, R.string.err_internet_default, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Launches the Date Picker Dialog Fragment
     */
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog();
        datePicker.setDateChangeListener(this);
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Called after selecting a date from the date picker
     *
     * @param epoch The epoch timestamp at the selected date
     */
    @Override
    public void onDateSet(long epoch) {
        // Display the date in DateOfBirth EditText and also calculate the age and display it
//        String formattedDate = Utils.convertEpochToDateString(epoch);
//        mDobTextView.setText(formattedDate);
//        mDob = formattedDate;
//        mEpochDob = epoch;
//        mIsUpdatesAvailable = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_doc_profile_sign_out:
                // Sign out
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                    finish();
                }
                break;
//            case R.id.tv_doc_profile_dob:
//                showDatePicker();
//                break;
        }
    }

    /**
     * Text Watcher for listening
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
//        mIsUpdatesAvailable = !mRegistrationNoField.getText().toString().equals("");
//        mIsUpdatesAvailable = !mDobTextView.getText().toString().equals("");
//        mIsUpdatesAvailable = !mExperienceField.getText().toString().equals("");
//        mIsUpdatesAvailable = !mHospitalField.getText().toString().equals("");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsUpdatesAvailable) {
            // Push the updates to the database
            pushToDatabase();
            mIsUpdatesAvailable = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsDataLoading || mIsDataLoadFailed) {
            // Data is loading OR Load: So don't show any errors if there is no data, because it's loading
            super.onBackPressed();
        }
    }
}