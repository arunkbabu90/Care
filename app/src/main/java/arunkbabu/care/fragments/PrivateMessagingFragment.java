package arunkbabu.care.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import arunkbabu.care.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivateMessagingFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.et_doc_message)
    EditText mMessageField;
    @BindView(R.id.ibtn_doc_call_patient)
    ImageButton mCallButton;
    @BindView(R.id.btn_doc_message_send)
    MaterialButton mMsgSendButton;

    private final String KEY_MESSAGE_CACHE = "key_message_cache";
    private final int KEY_PERMISSION_REQUEST_CALL_PHONE = 2000;

    private Activity mActivity;
    private String mMessage;
    private String mPatientNumber;

    public PrivateMessagingFragment(Activity activity) {
        // Required empty public constructor
        mActivity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_private_messaging, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPatientNumber = "+919539439554";

        mCallButton.setOnClickListener(this);
        mMsgSendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_doc_call_patient:
                placePhoneCall(mPatientNumber);
                break;

            case R.id.btn_doc_message_send:
                sendMsg();
                break;
        }
    }


    /**
     * Send the message to the patient
     */
    private void sendMsg() {
        Toast.makeText(mActivity, "Send Message", Toast.LENGTH_SHORT).show();
    }

    /**
     * Place a call to the given phone number via system phone app
     * @param phoneNumber The phone number to place a call
     */
    private void placePhoneCall(String phoneNumber) {
        Intent placeCallIntent = new Intent(Intent.ACTION_DIAL);
        placeCallIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (placeCallIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(placeCallIntent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_MESSAGE_CACHE, mMessage);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mMessage = savedInstanceState.getString(KEY_MESSAGE_CACHE);
            mMessageField.setText(mMessage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case KEY_PERMISSION_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Call Permission Granted
                    placePhoneCall(mPatientNumber);
                } else {
                    // Call Permission Denied
                    Toast.makeText(mActivity, "Call Permission is required. To place a call", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}