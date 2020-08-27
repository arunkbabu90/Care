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

public class ProcessingDialog extends DialogFragment {
    @BindView(R.id.tv_processing_dialog_message) MaterialTextView mMessageTextView;
    @BindView(R.id.tv_processing_dialog_button) TextView mButton;

    private Activity mActivity;
    private String mMessage;
    private String mButtonLabel;
    private boolean mIsDismissible;

    private ButtonClickListener mListener;

    /**
     * Creates a new instance of Processing Dialog.
     * @param activity The activity where this dialog belongs
     * @param message The message to show in the dialog beside the loading circle
     * @param buttonLabel The label of the button. If empty, the button will be disabled
     * @param isDismissible Whether the dialog is closable while clicking anywhere outside of its bounds
     */
    public ProcessingDialog(@NonNull Activity activity, @NonNull String message, @NonNull String buttonLabel, boolean isDismissible) {
        mActivity = activity;
        mMessage = message;
        mIsDismissible = isDismissible;
        mButtonLabel = buttonLabel;
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
        return inflater.inflate(R.layout.dialog_processing, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        // Whether the dialog is dismissible while clicking anywhere outside of its bounds
        setCancelable(mIsDismissible);

        if (mButtonLabel.equals("")) {
            mButton.setVisibility(View.INVISIBLE);
        }

        mButton.setText(mButtonLabel);
        mMessageTextView.setText(mMessage);

        // Brighten up the text view to show that it is activated
        mButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mButton.setTextColor(getResources().getColor(R.color.colorHoloOrangeActivated));
                    break;
                case MotionEvent.ACTION_UP:
                    mButton.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
            }
            return false;
        });

        mButton.setOnClickListener(v -> {
            // Add Click
            if (mListener != null)
                mListener.onButtonClick();
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
         * Called when a button in the dialog is clicked
         */
        void onButtonClick();
    }
}
