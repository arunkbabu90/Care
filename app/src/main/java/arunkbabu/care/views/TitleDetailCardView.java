package arunkbabu.care.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import arunkbabu.care.R;

public class TitleDetailCardView extends MaterialCardView {
    private String mTopText;
    private String mBottomText;
    private boolean mIsEditable;

    private MaterialTextView mTopTextView;
    private MaterialTextView mBottomTextView;
    private ImageView mEditIconView;

    public TitleDetailCardView(Context context) {
        super(context);
        initializeViews(context);
    }

    public TitleDetailCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TitleDetailCardView, 0, 0);
        try {
            mTopText = a.getString(R.styleable.TitleDetailCardView_topText);
            mBottomText = a.getString(R.styleable.TitleDetailCardView_bottomText);
            mIsEditable = a.getBoolean(R.styleable.TitleDetailCardView_editable, true);
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
            inflater.inflate(R.layout.view_title_detail_card, this, true);
            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            setRadius(radius);
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkBackgroundGrey1));

            // Populate the view with data

            mTopTextView = findViewById(R.id.title_detail_heading_text_view);
            mBottomTextView = findViewById(R.id.title_detail_description_text_view);
            mEditIconView = findViewById(R.id.title_detail_edit_icon_view);

            if (mIsEditable) {
                // Edits are ENABLED
                setClickable(true);
                setFocusable(true);
                mEditIconView.setVisibility(VISIBLE);
            } else {
                // Edits are DISABLED
                setClickable(false);
                setFocusable(false);
                mEditIconView.setVisibility(INVISIBLE);
            }

            mTopTextView.setText(mTopText);
            mBottomTextView.setText(mBottomText);
        } else {
            Toast.makeText(context, R.string.err_unexpected, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets the text string to the Top Text View
     * @param text String:  The text to be applied
     */
    public void setTopText(String text) {
        mTopTextView.setText(text);
    }

    /**
     * Returns the text in the Top Text View
     * @return String: The text
     */
    public String getTopText() {
        if (mTopTextView.getText() != null)
            return mTopTextView.getText().toString();
        else
            return "";
    }

    /**
     * Sets the text string to the Bottom Text View
     * @param text String:  The text to be applied
     */
    public void setBottomText(String text) {
        mBottomTextView.setText(text);
    }

    /**
     * Returns the text in the Bottom Text View
     * @return String: The text
     */
    public String getBottomText() {
        if (mBottomTextView.getText() != null)
            return mBottomTextView.getText().toString();
        else
            return "";
    }

    /**
     * Disables touch events to this view & hides the edit icon to indicate that to won't be editable
     */
    public void disableEdits() {
        mIsEditable = false;
        setClickable(false);
        setEnabled(false);
        mEditIconView.setVisibility(INVISIBLE);
    }
}