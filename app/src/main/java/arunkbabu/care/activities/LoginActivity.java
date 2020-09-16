package arunkbabu.care.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.views.CustomInputTextField;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener,
        View.OnTouchListener, OnCompleteListener<DocumentSnapshot> {
    @BindView(R.id.tv_login_error) TextView mErrorTextView;
    @BindView(R.id.et_email) CustomInputTextField mEmailField;
    @BindView(R.id.et_password) CustomInputTextField mPasswordField;
    @BindView(R.id.btn_login) MaterialButton mLoginButton;
    @BindView(R.id.tv_forgot_password) TextView mForgotPasswordTextView;
    @BindView(R.id.tv_sign_up) TextView mSignUpTextView;
    @BindView(R.id.pb_login) ProgressBar mLoginProgressBar;

    private final String TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private ConnectivityManager mConnectivityManager;
    private FirebaseFirestore mDb;
    private FirebaseUser mUser;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        // Get instances
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mUser = mAuth.getCurrentUser();
        if (mUser != null && Utils.isNetworkConnected(this)) {
            // Login the user, if already logged in
            mUser.reload()
                    .addOnSuccessListener(aVoid -> {
                        // User logged in so fetch user data from database to check whether its a doctor
                        // The result will be available in onComplete() callback
                        // REFRESH the User
                        mUser = mAuth.getCurrentUser();
                        if (mUser != null) {
                            mDb.collection(Constants.COLLECTION_USERS)
                                    .document(mUser.getUid())
                                    .get().addOnCompleteListener(LoginActivity.this);
                        }
                    });
        } else if (mUser != null && !Utils.isNetworkConnected(this)) {
            // If the user is already logged in even though there is no network, load the profile
            // from cache
            mDb.collection(Constants.COLLECTION_USERS).document(mUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Long userType = documentSnapshot.getLong(Constants.FIELD_USER_TYPE);
                        startCorrectActivity(userType);
                    });
        } else {
            // User not logged in. So show the login screen
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_login);
            ButterKnife.bind(this);

            mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            // Register a callback to track internet connectivity changes
            registerNetworkChangeCallback();
            if (!Utils.isNetworkConnected(this)) {
                // Network NOT Connected
                mErrorTextView.setText(getString(R.string.err_no_internet));
                mErrorTextView.setVisibility(View.VISIBLE);
                mLoginButton.setClickable(false);
                mForgotPasswordTextView.setClickable(false);
                mForgotPasswordTextView.setEnabled(false);
            }

            // Make the text field a password field here rather that in the XML to successfully apply
            //  the custom font; because setting it in XML won't work
            mPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswordField.setTypeface(mEmailField.getTypeface());
            mPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mPasswordField.setOnFocusChangeListener(this);

            mForgotPasswordTextView.setOnTouchListener(this);
            mSignUpTextView.setOnTouchListener(this);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_password: {
                if (hasFocus) {
                    // Check whether the email isn't blank or is valid
                    checkEmail();
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.tv_forgot_password) {
                    mForgotPasswordTextView.setTextColor(getResources().getColor(R.color.colorViolet));
                } else if (v.getId() == R.id.tv_sign_up) {
                    mSignUpTextView.setTextColor(getResources().getColor(R.color.colorViolet));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.tv_forgot_password) {
                    mForgotPasswordTextView.setTextColor(getResources().getColor(R.color.colorLightIndigoNormal));
                } else if (v.getId() == R.id.tv_sign_up) {
                    mSignUpTextView.setTextColor(getResources().getColor(R.color.colorLightIndigoNormal));
                }
                v.performClick();
                break;
        }
        return false;
    }


    /**
     * Login Button click
     */
    public void onLoginClick(View view) {
        if (checkEmail() && checkPassword()) {
            mLoginButton.setClickable(false);
            mLoginProgressBar.setVisibility(View.VISIBLE);

            // Hide virtual keyboard
            Utils.closeSoftInput(this);

            Editable emailText = mEmailField.getText();
            String email = "";
            if (emailText != null) email = emailText.toString();

            Editable passText = mPasswordField.getText();
            String password = "";
            if (passText != null) password = passText.toString();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Login Success
                    mErrorTextView.setVisibility(View.INVISIBLE);

                    // Fetch user data from database to check whether its a doctor
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // The result will be available in onComplete() callback
                        mDb.collection(Constants.COLLECTION_USERS)
                                .document(user.getUid())
                                .get().addOnCompleteListener(LoginActivity.this);
                    }
                } else {
                    // Login Failure
                    mErrorTextView.setText(getString(R.string.err_login));
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mLoginProgressBar.setVisibility(View.GONE);
                    mLoginButton.setClickable(true);
                }
            });
        }
    }

    /**
     * Forgot Password TextView Click
     */
    public void onForgotPasswordClick(View view) {
        // Launch Forgot Password Activity
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    /**
     * Sign up Text View click
     */
    public void onSignUpTextViewClick(View view) {
        // Launch SignUp Activity
        startActivity(new Intent(this, SignUpActivity.class));
    }

    /**
     * Check whether the email is valid or not empty
     * @return True If it is valid
     */
    private boolean checkEmail() {
        Editable emailText = mEmailField.getText();
        String email = "";
        if (emailText != null) {
            email = emailText.toString();
        }

        if (email.matches("")) {
            // Email is blank
            mEmailField.setError(getResources().getString(R.string.err_blank_email));
            return false;
        } else if (Utils.verifyEmail(email)) {
            // Invalid Email
            mEmailField.setError(getResources().getString(R.string.err_invalid_email));
            return false;
        }
        // Email is correct
        return true;
    }

    /**
     * Checks whether the password is not empty
     * @return True If it is not empty
     */
    private boolean checkPassword() {
        String password = Objects.requireNonNull(mPasswordField.getText()).toString();
        if (password.matches("")) {
            // Password is blank
            mPasswordField.setError(getResources().getString(R.string.err_password));
            return false;
        }
        // Password is correct
        return true;
    }

    /**
     * Register a callback to be invoked when network connectivity changes
     * @return True If internet is available; False otherwise
     */
    private boolean registerNetworkChangeCallback() {
        final boolean[] isAvailable = new boolean[1];

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        if (mConnectivityManager != null) {
            mConnectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    // Internet is Available
                    runOnUiThread(() -> {
                        mErrorTextView.setText("");
                        mErrorTextView.setVisibility(View.INVISIBLE);
                        mLoginButton.setClickable(true);
                        mForgotPasswordTextView.setClickable(true);
                        mForgotPasswordTextView.setEnabled(true);

                        // In-case if the user is already logged in, perform auto-login
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            mLoginProgressBar.setVisibility(View.VISIBLE);
                            mDb.collection(Constants.COLLECTION_USERS).document(user.getUid()).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        Long userType = documentSnapshot.getLong(Constants.FIELD_USER_TYPE);
                                        startCorrectActivity(userType);
                                        mLoginProgressBar.setVisibility(View.GONE);
                                    });
                        }
                    });
                    isAvailable[0] = true;
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    // Internet is Unavailable
                    isAvailable[0] = false;

                    runOnUiThread(() -> {
                        mErrorTextView.setText(getString(R.string.err_no_internet));
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mLoginButton.setClickable(false);
                        mForgotPasswordTextView.setClickable(false);
                        mForgotPasswordTextView.setEnabled(false);
                    });
                }
            });
        }

        return isAvailable[0];
    }

    /**
     * Launches DoctorActivity or Patient based on the type of user signed in
     * @param userType The type of the user {Patient, Doctor, ADMIN}
     */
    private void startCorrectActivity(Long userType) {
        if (mLoginProgressBar != null) mLoginProgressBar.setVisibility(View.GONE);
        if (mLoginButton != null) mLoginButton.setClickable(true);

        int uType = userType.intValue();

        switch (uType) {
            case Constants.USER_TYPE_PATIENT:
                // The user is a Patient
                startActivity(new Intent(this, PatientActivity.class));
                break;
            case Constants.USER_TYPE_DOCTOR:
                // The user is a doctor
                startActivity(new Intent(this, DoctorActivity.class));
                break;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Database query result will be available here
     * @param task Contains the database query result
     */
    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document != null) {
                if (document.exists()) {
                    // Get whether the user is a doctor from Db
                    Long isDoctor = document.getLong(Constants.FIELD_USER_TYPE);
                    // Start the activity for the user
                    startCorrectActivity(isDoctor);
                }
            }
        }
    }
}