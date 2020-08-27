package arunkbabu.care.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textview.MaterialTextView;

import arunkbabu.care.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorDialog extends DialogFragment {
    @BindView(R.id.tv_error_dialog_message) MaterialTextView mMessageTextView;
    @BindView(R.id.tv_error_dialog_negative) TextView mNegativeButton;
    @BindView(R.id.tv_error_dialog_positive) TextView mPositiveButton;

    private Activity mActivity;
    private String mMessage;
    private String mPositiveButtonLabel;
    private String mNegativeButtonLabel;

    private ButtonClickListener mListener;

    /**
     * Creates a new instance of Error Dialog.
     * @param activity The activity where this dialog belongs
     * @param message The message to show in the dialog
     * @param positiveButtonLabel The label of the positive button. If empty, a default label will be used
     * @param negativeButtonLabel The label of the negative button. If empty, the button will be disabled
     */
    public ErrorDialog(Activity activity, String message, @NonNull String positiveButtonLabel,
                       @NonNull String negativeButtonLabel) {
        mActivity = activity;
        mMessage = message;

        if (!positiveButtonLabel.equals("")) {
            mPositiveButtonLabel = positiveButtonLabel;
        } else {
            mPositiveButtonLabel = "";
        }

        if (!negativeButtonLabel.equals("")) {
            mNegativeButtonLabel = negativeButtonLabel;
        } else {
            mNegativeButtonLabel = "";
        }
    }

    /**
     * Creates a new instance of Error Dialog.
     * @param activity The activity where this dialog belongs
     * @param message The message to show in the dialog
     * @param positiveButtonLabel The label of the positive button. If empty, a default label will be used
     */
    public ErrorDialog(Activity activity, String message, @NonNull String positiveButtonLabel) {
        mActivity = activity;
        mMessage = message;

        if (!positiveButtonLabel.equals("")) {
            mPositiveButtonLabel = positiveButtonLabel;
        } else {
            mPositiveButtonLabel = "";
        }

        mNegativeButtonLabel = getString(R.string.cancel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            // Make the window transparent to set the round corner background drawable without
            // background artifacts
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.dialog_error, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (mPositiveButtonLabel.equals("")) {
            mPositiveButton.setVisibility(View.GONE);
        } else {
            mPositiveButton.setText(mPositiveButtonLabel);
        }

        if (mNegativeButtonLabel.equals("")) {
            mNegativeButton.setVisibility(View.GONE);
        } else {
            mNegativeButton.setText(mNegativeButtonLabel);
        }

        mMessageTextView.setText(mMessage);

        // Brighten up the text view to show that it is activated
        mPositiveButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPositiveButton.setTextColor(getResources().getColor(R.color.colorHoloGreenActivated));
                    break;
                case MotionEvent.ACTION_UP:
                    mPositiveButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
            }
            return false;
        });

        // Brighten up the text view to show that it is activated
        mNegativeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mNegativeButton.setTextColor(getResources().getColor(R.color.colorBlueActivated));
                    break;
                case MotionEvent.ACTION_UP:
                    mNegativeButton.setTextColor(getResources().getColor(R.color.colorBlue));
                    break;
            }
            return false;
        });

        mPositiveButton.setOnClickListener(v -> {
            // Add Click
            if (mListener != null)
                mListener.onPositiveButtonClick();
            dismiss();
        });

        mNegativeButton.setOnClickListener(v -> {
            // Cancel click
            if (mListener != null)
                mListener.onNegativeButtonClick();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Scale the dialog with respect to the screen size
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            getDialog().getWindow().setLayout(displayMetrics.widthPixels - 100, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Register a callback to be invoked when a button in the dialog is clicked
     * @param listener The callback that will be run
     */
    public void setButtonClickListener(ButtonClickListener listener) {
        mListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when a button in the dialog is clicked
     */
    public interface ButtonClickListener {
        /**
         * Called when positive button in the dialog is clicked
         */
        void onPositiveButtonClick();
        /**
         * Called when negative button in the dialog is clicked
         */
        void onNegativeButtonClick();
    }
}
