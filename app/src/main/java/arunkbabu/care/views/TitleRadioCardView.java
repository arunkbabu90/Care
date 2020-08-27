package arunkbabu.care.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;

import arunkbabu.care.R;

public class TitleRadioCardView extends MaterialCardView {
    private MaterialTextView mTitleTextView;
    private MaterialRadioButton mRadioButton1;
    private MaterialRadioButton mRadioButton2;

    private String mTitleText;
    private String mRadioButton1Text;
    private String mRadioButton2Text;
    private boolean mIsRadio1Checked;
    private boolean mIsRadio2Checked;

    private OnCheckedStateChangeListener mListener;

    public TitleRadioCardView(Context context) {
        super(context);
        initializeViews(context);
    }

    public TitleRadioCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TitleRadioCardView,0,0);
        try {
            mTitleText = a.getString(R.styleable.TitleRadioCardView_headingText);
            mRadioButton1Text = a.getString(R.styleable.TitleRadioCardView_radio1Text);
            mRadioButton2Text = a.getString(R.styleable.TitleRadioCardView_radio2Text);
            mIsRadio1Checked = a.getBoolean(R.styleable.TitleRadioCardView_radio1Checked, false);
            mIsRadio2Checked = a.getBoolean(R.styleable.TitleRadioCardView_radio2Checked, false);
        } finally {
            a.recycle();
        }
        initializeViews(context);
    }

    /**
     * Inflates the layout and set its properties
     * @param context The Context where this view belongs
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.view_title_radio_card, this, true);
            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            setRadius(radius);
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkBackgroundGrey1));

            mTitleTextView = findViewById(R.id.title_radio_heading_text_view);
            mRadioButton1 = findViewById(R.id.title_radio_radio_button_1);
            mRadioButton2 = findViewById(R.id.title_radio_radio_button_2);

            mTitleTextView.setText(mTitleText);
            mRadioButton1.setText(mRadioButton1Text);
            mRadioButton2.setText(mRadioButton2Text);
            mRadioButton1.setChecked(mIsRadio1Checked);
            mRadioButton2.setChecked(mIsRadio2Checked);

            mRadioButton1.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onCheckChange(mRadioButton1.isChecked(), true);
                }
            });

            mRadioButton2.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onCheckChange(mRadioButton2.isChecked(), false);
                }
            });
        }
    }

    /**
     * Sets the text for the Title Text View
     * @param text The text to be applied
     */
    public void setTitleText(String text) {
        mTitleTextView.setText(text);
    }

    /**
     * Sets the text for the Radio Button 1
     * @param text The text to be applied
     */
    public void setRadioButton1Text(String text) {
        mRadioButton1.setText(text);
    }

    /**
     * Sets the text for the Radio Button 2
     * @param text The text to be applied
     */
    public void setRadioButton2Text(String text) {
        mRadioButton2.setText(text);
    }

    /**
     * Changes the Checked state of Radio Button 1
     * @param checked boolean: true to check the button, false to uncheck it
     */
    public void setRadio1Checked(boolean checked) {
        mRadioButton1.setChecked(checked);
    }

    /**
     * Changes the Checked state of Radio Button 2
     * @param checked boolean: true to check the button, false to uncheck it
     */
    public void setRadio2Checked(boolean checked) {
        mRadioButton2.setChecked(checked);
    }

    /**
     * Returns whether the Radio Button 1 is checked
     * @return True if checked; False if unchecked
     */
    public boolean isRadio1Checked() {
        return mRadioButton1.isChecked();
    }

    /**
     * Returns whether the Radio Button 2 is checked
     * @return True if checked; False if unchecked
     */
    public boolean isRadio2Checked() {
        return mRadioButton2.isChecked();
    }

    /**
     * Register a callback to be invoked when an item is swiped off
     * @param listener The callback that will be run
     */
    public void setCheckChangeListener(OnCheckedStateChangeListener listener) {
        mListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the check state of a Radio button changes
     */
    public interface OnCheckedStateChangeListener {
        /**
         * Called when the check state of a Radio button changes
         * @param checked true: if checked; false: if unchecked
         * @param isRadio1 Whether checked state changed is Radio Button 1
         */
        void onCheckChange(boolean checked, boolean isRadio1);
    }
}