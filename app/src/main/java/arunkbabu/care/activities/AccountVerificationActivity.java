 package arunkbabu.care.activities;

 import android.content.Intent;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;

 import androidx.appcompat.app.AppCompatActivity;
 import androidx.fragment.app.Fragment;

 import com.google.android.material.button.MaterialButton;
 import com.google.android.material.textview.MaterialTextView;

 import arunkbabu.care.R;
 import arunkbabu.care.fragments.signup.VerificationEmailFragment;
 import butterknife.BindView;
 import butterknife.ButterKnife;

 public class AccountVerificationActivity extends AppCompatActivity {
     @BindView(R.id.btn_account_verification_next) MaterialButton mNextButton; /* Retry button */
     @BindView(R.id.tv_account_verification_countdown) MaterialTextView mCountdownTextView;

     public static final String KEY_USER_EMAIL = "keyUserEmailAccountVerification";
     public static final String KEY_USER_PHONE_NUMBER = "keyUserPhoneNumberAccountVerification";
     public static final String KEY_USER_PASSWORD = "keyUserPasswordAccountVerification";
     public static final String KEY_BACK_BUTTON_BEHAVIOUR = "keyBackButtonBehaviour";

     /**
      * Close this activity when back button is pressed
      * Use with {@link #KEY_BACK_BUTTON_BEHAVIOUR} or {@link #mBackButtonBehaviour}
      */
     public static final int BEHAVIOUR_CLOSE = 11000;

     /**
      * Launches the main Dashboard Screen when back button is pressed
      * Use with {@link #KEY_BACK_BUTTON_BEHAVIOUR} or {@link #mBackButtonBehaviour}
      */
     public static final int BEHAVIOUR_LAUNCH_DASHBOARD = 11001;

     private static String mPhoneNumber;
     private static String mEmail;
     private static String mPassword;
     private int mBackButtonBehaviour;

     private Fragment mFragment;
     private CountDownTimer mCountdownTimer;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_account_verification);
         ButterKnife.bind(this);

         mEmail = getIntent().getStringExtra(KEY_USER_EMAIL);
         mPhoneNumber = getIntent().getStringExtra(KEY_USER_PHONE_NUMBER);
         mPassword = getIntent().getStringExtra(KEY_USER_PASSWORD);
         mBackButtonBehaviour = getIntent().getIntExtra(KEY_BACK_BUTTON_BEHAVIOUR, BEHAVIOUR_CLOSE);

         mFragment = new VerificationEmailFragment(this, this);
         getSupportFragmentManager().beginTransaction()
                 .add(R.id.account_verification_fragment_container, mFragment)
                 .commit();

         // Start a countdown timer for showing the time remaining before the retry button appears
         mCountdownTextView.setVisibility(View.VISIBLE);
         mNextButton.setVisibility(View.INVISIBLE);
         mCountdownTimer = new CountDownTimer(60000, 1000) {
             @Override
             public void onTick(long millisUntilFinished) {
                 mCountdownTextView.setText(getString(R.string.wait_n_seconds_to_retry, String.valueOf(millisUntilFinished / 1000)));
             }

             @Override
             public void onFinish() {
                 mCountdownTextView.setVisibility(View.INVISIBLE);
                 // Reveal the RETRY button after 60 seconds
                 mNextButton.setVisibility(View.VISIBLE);
             }
         }.start();
     }

     /**
      * Makes the RETRY Button visible
      */
     public void makeRetryButtonVisible() {
         if (mCountdownTimer != null) {
             mCountdownTimer.cancel();
         }
         mCountdownTextView.setVisibility(View.INVISIBLE);
         // Reveal the RETRY button after 60 seconds
         mNextButton.setVisibility(View.VISIBLE);
         if (mFragment instanceof VerificationEmailFragment) {
             // The button is now RETRY Button
             if (((VerificationEmailFragment) mFragment).isAccountVerified()) {
                 mNextButton.setText(R.string._continue);
             } else {
                 mNextButton.setText(R.string.retry);
             }
         }
     }

     /**
      * Called when the Next Button in this activity is clicked
      * @param view The view clicked
      */
     public void onVerificationNextClick(View view) {
         // Send the verification email again
         if (mFragment instanceof VerificationEmailFragment) {
             if (((VerificationEmailFragment) mFragment).isAccountVerified()) {
                 // Button is now CONTINUE button
                 // So Login to the account
                 if (mBackButtonBehaviour == BEHAVIOUR_CLOSE) {
                     finish();
                 } else if (mBackButtonBehaviour == BEHAVIOUR_LAUNCH_DASHBOARD) {
                     Intent launchLogin = new Intent(this, LoginActivity.class);
                     launchLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(launchLogin);
                     finish();
                 }
             } else {
                 boolean isInProgress = ((VerificationEmailFragment) mFragment).sendVerificationEmail(mEmail);
                 // This check is used to keep the Retry button visible if an error occurs; so that
                 // users can retry immediately without waiting for 60 seconds
                 if (isInProgress) {
                     // Hide the RETRY Button for 40 seconds
                     mNextButton.setVisibility(View.INVISIBLE);
                     // Start a countdown timer for showing the time remaining before the retry button appears
                     mCountdownTextView.setVisibility(View.VISIBLE);
                     if (mCountdownTimer != null) {
                         mCountdownTimer.cancel();
                     }
                     mCountdownTimer = new CountDownTimer(60000, 1000) {
                         @Override
                         public void onTick(long millisUntilFinished) {
                             mCountdownTextView.setText(getString(R.string.wait_n_seconds_to_retry, String.valueOf(millisUntilFinished / 1000)));
                         }

                         @Override
                         public void onFinish() {
                             mCountdownTextView.setVisibility(View.INVISIBLE);
                             // Reveal the RETRY button after 60 seconds
                             mNextButton.setVisibility(View.VISIBLE);
                         }
                     }.start();
                 }
             }
         }
     }

     @Override
     public void onBackPressed() {
         switch (mBackButtonBehaviour) {
             case BEHAVIOUR_CLOSE:
                 super.onBackPressed();
                 break;
             case BEHAVIOUR_LAUNCH_DASHBOARD:
                 // Launch the dashboard with the logged in user
                 Intent launchLogin = new Intent(this, LoginActivity.class);
                 launchLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(launchLogin);
                 finish();
                 break;
         }
     }

     public static String getUserEmail() {
         return mEmail;
     }

     public static String getUserPhoneNumber() {
         return mPhoneNumber;
     }

     public static String getPassword() {
         return mPassword;
     }
}