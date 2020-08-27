package arunkbabu.care.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import arunkbabu.care.R;

public class CustomInputTextField extends TextInputEditText {

    public CustomInputTextField(Context context) {
        super(context);
        init(context);
    }

    public CustomInputTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomInputTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTextColor(ContextCompat.getColor(context, android.R.color.white));
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.transparent_input_field_drawable));
    }
}
