package arunkbabu.care.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import arunkbabu.care.R;

/**
 * A custom button which changes its state if touched keeping its state as activated with a visual feedback
 * until a next touch event occurs
 */
public class CheckedButton extends MaterialButton {
    int[][] state = new int[][] { new int[] { android.R.attr.state_enabled} };
    int[] colorActivated = new int[] { ContextCompat.getColor(getContext(), android.R.color.holo_green_light) };
    int[] colorNotActivated = new int[] { ContextCompat.getColor(getContext(), R.color.colorDocProfileButton) };

    private boolean mIsActivated;

    public CheckedButton(@NonNull Context context) {
        super(context);
    }

    public CheckedButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckedButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        // Button is clicked so change its activated state and give a visual feedback
        if (mIsActivated) {
            // Button is already activated; So deactivate it & revert its color back to default
            setBackgroundTintList(new ColorStateList(state, colorNotActivated));
            mIsActivated = false;
        } else {
            // Button is NOT activated; So activate it & change its color to activated color
            setBackgroundTintList(new ColorStateList(state, colorActivated));
            mIsActivated = true;
        }
        return super.performClick();
    }

    /**
     * Set the activated state of the button
     * @param isActivated True: button needs to be activated; false otherwise
     */
    public void setIsActivated(boolean isActivated) {
        this.mIsActivated = isActivated;
    }

    /**
     * Get the activated state of the button
     * @return True: if the button is activated; false otherwise
     */
    public boolean getIsActivated() {
        return mIsActivated;
    }
}
