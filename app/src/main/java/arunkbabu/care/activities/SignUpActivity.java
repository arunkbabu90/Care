package arunkbabu.care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.dialogs.ErrorDialog;
import arunkbabu.care.dialogs.ProcessingDialog;
import arunkbabu.care.fragments.signup.SignUpDoctorFragment;
import arunkbabu.care.fragments.signup.SignUpDoctorSpecialityFragment;
import arunkbabu.care.fragments.signup.SignUpMainFragment;
import arunkbabu.care.fragments.signup.SignUpPatientFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements ErrorDialog.ButtonClickListener {
    @BindView(R.id.btn_sign_up_next) MaterialButton mNextButton;

    private int mErrorCase = Constants.NULL_INT;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private SignUpPatientFragment mPatientFrag;
    private SignUpDoctorFragment mDoctorDetailsFrag;
    private SignUpDoctorSpecialityFragment mDoctorSpecialityFrag;
    private SignUpMainFragment mMainFrag;
    private ProcessingDialog mProcessingDialog;

    private boolean mIsDoctor, mIsSigningUp;
    private String mFullName;
    private String mEmail;
    private String mPassword;
    private String mContactNumber;
    private String mDocRegId;
    private String mQualifications;
    private String mSpeciality;
    private int mUserType;

    /**
     * Error case occurred during adding information to user's profile
     */
    public static final int CASE_UPDATE_PROFILE = 5000;

    /**
     * Error case occurred while adding information to user's database
     */
    public static final int CASE_PUSH_DETAILS = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mMainFrag = new SignUpMainFragment();
        mPatientFrag = new SignUpPatientFragment();
        mDoctorDetailsFrag = new SignUpDoctorFragment();
        mDoctorSpecialityFrag = new SignUpDoctorSpecialityFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.sign_up_fragment_container, mMainFrag)
                .commit();
    }

    /**
     * Creates a user account and signs up the user
     */
    private void performSignUp() {
        if (mPatientFrag != null) {
            if (!mPatientFrag.checkAllFields()) {
                // Field(s) are empty
                Toast.makeText(this, R.string.err_pls_fix_all_errors, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (mDoctorDetailsFrag != null) {
            if (!mDoctorDetailsFrag.checkAllFields()) {
                // Field(s) are empty
                Toast.makeText(this, R.string.err_pls_fix_all_errors, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if ((mDoctorSpecialityFrag != null && mSpeciality.isEmpty()) || (mDoctorSpecialityFrag != null && mQualifications.isEmpty())) {
            Toast.makeText(this, R.string.err_some_field_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        mIsSigningUp = true;
        mProcessingDialog = showProcessingDialog(getString(R.string.creating_profile), "");

        if (mPatientFrag != null && SignUpPatientFragment.signUpPatientFragActive) {
            // Patient SignUp
            mFullName = mPatientFrag.getFullName();
            mEmail = mPatientFrag.getEmail();
            mPassword = mPatientFrag.getPassword();
            mContactNumber = mPatientFrag.getMobileNumber();
            mUserType = Constants.USER_TYPE_PATIENT;
        } else if (mDoctorDetailsFrag != null && SignUpDoctorFragment.signUpDoctorFragActive) {
            // Doctor SignUp
            mFullName = mDoctorDetailsFrag.getFullName();
            mEmail = mDoctorDetailsFrag.getEmail();
            mPassword = mDoctorDetailsFrag.getPassword();
            mContactNumber = mDoctorDetailsFrag.getMobileNumber();
            mDocRegId = mDoctorDetailsFrag.getRegisterId();
            mUserType = Constants.USER_TYPE_DOCTOR;
        }

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnSuccessListener(authResult -> {
                    // User Created
                    // Make sure the user is signed in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // Update the user's account with details first
                        updateProfile(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // User Creation Failure
                    mIsSigningUp = false;
                });
    }

    /**
     * Helper method to push all the details the user entered to the database
     * @param user The Firebase user instance
     */
    private void pushDetailsToDatabase(FirebaseUser user) {
        // If any of the fields are empty don't attempt sign up
        if (mFullName.equals("") || mEmail.equals("") || mPassword.equals("") || mContactNumber.equals("")) {
            return;
        }
        if (mUserType == Constants.USER_TYPE_DOCTOR) {
            if (mDocRegId.equals("")) {
                return;
            }
        }
        // Create database with the UID and fill up all the details to the user's
        // profile database document
        String uid = user.getUid();

        Map<String, Object> ud = new HashMap<>();
        ud.put(Constants.FIELD_FULL_NAME, mFullName);
        ud.put(Constants.FIELD_CONTACT_NUMBER, mContactNumber);
        ud.put(Constants.FIELD_USER_TYPE, mUserType);
        ud.put(Constants.FIELD_ACCOUNT_VERIFIED, false); // Initialize Verification status to false always
        // If the user is a doctor then upload his/her registered id too
        if (mUserType == Constants.USER_TYPE_DOCTOR) {
            ud.put(Constants.FIELD_DOC_REG_ID, mDocRegId);
            ud.put(Constants.FIELD_DOCTOR_SPECIALITY, mSpeciality);
            ud.put(Constants.FIELD_DOCTOR_QUALIFICATIONS, mQualifications);
        }

        mDb.collection(Constants.COLLECTION_USERS)
                .document(uid).set(ud)
                .addOnSuccessListener(aVoid -> {
                    // Information successfully added to database
                    if (mProcessingDialog != null) {
                        mProcessingDialog.dismiss();
                    }

                    // Initiate Account verification
                    Intent i = new Intent(this, AccountVerificationActivity.class);
                    if (mUserType == Constants.USER_TYPE_PATIENT) {
                        i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, mPatientFrag.getEmail());
                        i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, mPatientFrag.getMobileNumber());
                        i.putExtra(AccountVerificationActivity.KEY_USER_PASSWORD, mPatientFrag.getPassword());
                    } else if (mUserType == Constants.USER_TYPE_DOCTOR){
                        i.putExtra(AccountVerificationActivity.KEY_USER_EMAIL, mDoctorDetailsFrag.getEmail());
                        i.putExtra(AccountVerificationActivity.KEY_USER_PHONE_NUMBER, mDoctorDetailsFrag.getMobileNumber());
                        i.putExtra(AccountVerificationActivity.KEY_USER_PASSWORD, mDoctorDetailsFrag.getPassword());
                    }
                    i.putExtra(AccountVerificationActivity.KEY_BACK_BUTTON_BEHAVIOUR, AccountVerificationActivity.BEHAVIOUR_LAUNCH_DASHBOARD);
                    startActivity(i);
                    finish();

                    mIsSigningUp = false;
                })
                .addOnFailureListener(e -> {
                    // Error while adding information to database
                    mErrorCase = CASE_PUSH_DETAILS;
                    if (mProcessingDialog != null) {
                        mProcessingDialog.dismiss();
                    }
                    mIsSigningUp = false;

                    Toast.makeText(this, R.string.err_profile_add_details_failed, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Helper method to perform updateProfile(profileUpdateRequest)
     * @param user The FirebaseUser
     */
    private void updateProfile(FirebaseUser user) {
        String fullName = "";
        if (mUserType == Constants.USER_TYPE_PATIENT) {
            fullName = mPatientFrag.getFullName();
        } else if (mUserType == Constants.USER_TYPE_DOCTOR){
            fullName = mDoctorDetailsFrag.getFullName();
        }

        if (!fullName.equals("")) {
            UserProfileChangeRequest profileUpdateRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();

            user.updateProfile(profileUpdateRequest)
                    .addOnSuccessListener(aVoid -> {
                        pushDetailsToDatabase(user);
                    })
                    .addOnFailureListener(e -> {
                        // Show error with retry button
                        showErrorDialog(getString(R.string.err_profile_add_details_failed),
                                getString(R.string.retry));
                        mErrorCase = CASE_UPDATE_PROFILE;
                        mIsSigningUp = false;
                    });
        } else {
            Toast.makeText(this, R.string.err_some_field_empty, Toast.LENGTH_SHORT).show();
            mIsSigningUp = false;
        }
    }


    /**
     * Show the error dialog
     * @param message The error message to be shown
     * @param positiveButtonLabel The label of the positive button
     * @return An instance of the ErrorDialog Fragment
     */
    private ErrorDialog showErrorDialog(@NonNull String message, String positiveButtonLabel) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ErrorDialog dialog = new ErrorDialog(this, message, positiveButtonLabel);
        dialog.setButtonClickListener(this);
        dialog.show(ft, "dialog");
        return dialog;
    }

    /**
     * Show the processing dialog
     * @param message The message to be shown beside the loading circle
     * @param buttonLabel The label of the button. If empty, the button will be disabled
     * @return An instance of the ProcessingDialog Fragment
     */
    private ProcessingDialog showProcessingDialog(@NonNull String message, @NonNull String buttonLabel) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ProcessingDialog dialog = new ProcessingDialog(this, message, buttonLabel, false);
        dialog.show(ft, "dialog");
        return dialog;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Hide the soft input
        Utils.closeSoftInput(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Delete account if there is an error in pushing data to database
        if (mErrorCase != Constants.NULL_INT) {
            // If the mErrorCase is not NULL_INT there is an error
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.delete();
            }
        }
    }

    /**
     * Positive Button Click Event of ErrorDialog
     */
    @Override
    public void onPositiveButtonClick() {
        FirebaseUser user = mAuth.getCurrentUser();
        switch (mErrorCase) {
            case CASE_UPDATE_PROFILE:
                if (user != null) {
                    updateProfile(user);
                    Toast.makeText(this, R.string.retrying_profile_update, Toast.LENGTH_SHORT).show();
                }
                break;
            case CASE_PUSH_DETAILS:
                if (user != null)
                    pushDetailsToDatabase(user);

                mErrorCase = Constants.NULL_INT;
                break;
        }
    }

    /**
     * Negative Button Click Event of ErrorDialog
     */
    @Override
    public void onNegativeButtonClick() { }


    /**
     * Invoked when the Next button is clicked
     * @param view The view that is clicked
     */
    public void onNextClick(View view) {
        // Load the next page
        if (SignUpMainFragment.signUpMainFragActive) {
            mUserType = mMainFrag.getSelectedUserType();
        }
        switch (mUserType) {
            case Constants.USER_TYPE_PATIENT:
                if (mNextButton.getText().equals("Create")) {
                    // Create button behaviour
                    performSignUp();
                }
                mDoctorSpecialityFrag = null;
                mDoctorDetailsFrag = null;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.sign_up_fragment_container, mPatientFrag)
                        .commit();
                // Next Button Behaviour
                mNextButton.setText(getString(R.string.create));
                break;
            case Constants.USER_TYPE_DOCTOR:
                // Navigate to the Appropriate page when Next is pressed or Create account if on last page
                mPatientFrag = null;
                Fragment frag = null;
                if (SignUpMainFragment.signUpMainFragActive) {
                    // Goto Next page: DoctorSpecialitySelection
                    frag = mDoctorSpecialityFrag;
                } else if (SignUpDoctorSpecialityFragment.signUpSpecialityFragActive) {
                    // Goto Next page: DoctorDetailsEntry
                    if (mDoctorSpecialityFrag.checkAllFields()) {
                        // All fields filled so cache it in here & Go to Next Page
                        mSpeciality = mDoctorSpecialityFrag.getSpeciality();
                        mQualifications = mDoctorSpecialityFrag.getQualifications();
                        frag = mDoctorDetailsFrag;
                        // Next Button Behaviour
                        mNextButton.setText(getString(R.string.create));
                    }
                } else {
                    // Create button behaviour
                    performSignUp();
                }
                if (frag != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.sign_up_fragment_container, frag)
                            .commit();
                }
                break;
        }
    }
}