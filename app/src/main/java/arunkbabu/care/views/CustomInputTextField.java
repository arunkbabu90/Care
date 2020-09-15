package arunkbabu.care.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import arunkbabu.care.R;

public class CustomInputTextField extends TextInputEditText {
    private int mTextColor;

    public CustomInputTextField(Context context) {
        super(context);
        init(context);
    }

    public CustomInputTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomInputTextField, 0,0);

        try {
            mTextColor = a.getColor(R.styleable.CustomInputTextField_android_textColor, ContextCompat.getColor(context, android.R.color.white));
        } finally {
            a.recycle();
        }
        init(context);
    }

    public CustomInputTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTextColor(mTextColor);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.transparent_drawable));
    }
}
