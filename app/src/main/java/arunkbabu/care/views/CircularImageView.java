package arunkbabu.care.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import arunkbabu.care.R;

public class CircularImageView extends MaterialCardView {
    public static final int SCALE_TYPE_MATRIX = 0;
    public static final int SCALE_TYPE_FITXY = 1;
    public static final int SCALE_TYPE_FITSTART = 2;
    public static final int SCALE_TYPE_FITCENTER = 3;
    public static final int SCALE_TYPE_FITEND = 4;
    public static final int SCALE_TYPE_CENTER = 5;
    public static final int SCALE_TYPE_CENTERCROP = 6;
    public static final int SCALE_TYPE_CENTERINSIDE = 7;

    private ImageView mImageView;
    private int mDpResourceId;
    private int mScaleType;
    private int mIconTint;
    private boolean mIsInflationSuccess;

    public CircularImageView(Context context) {
        super(context);
        initializeViews(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularImageView, 0,0);

        try {
            mDpResourceId = a.getResourceId(R.styleable.CircularImageView_src, -1);
            mScaleType = a.getInteger(R.styleable.CircularImageView_scaleType, 3);
            mIconTint = a.getColor(R.styleable.CircularImageView_iconTint, ContextCompat.getColor(context, android.R.color.transparent));
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
            inflater.inflate(R.layout.view_circular_image, this, true);
            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
            setRadius(radius); // Card Radius
            setCardBackgroundColor(getResources().getColor(R.color.colorBackgroundGrey)); // Background Color
            mIsInflationSuccess = true;
        } else {
            Toast.makeText(context, getResources().getString(R.string.err_unexpected), Toast.LENGTH_LONG).show();
            mIsInflationSuccess = false;
        }
    }

    /**
     * Helper method to convert the supplied integer scale type to its related enum
     * Controls how the image should be resized or moved to match the size of this ImageView
     * @param scaleType int: The scale type for the image
     */
    public void setScaleType(int scaleType) {
        switch (scaleType) {
            case 0:
                mImageView.setScaleType(ImageView.ScaleType.MATRIX);
                break;
            case 1:
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            case 2:
                mImageView.setScaleType(ImageView.ScaleType.FIT_START);
                break;
            case 3:
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case 4:
                mImageView.setScaleType(ImageView.ScaleType.FIT_END);
                break;
            case 5:
                mImageView.setScaleType(ImageView.ScaleType.CENTER);
                break;
            case 6:
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 7:
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                break;
            default:
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
        }
    }

    /**
     * Set the image from image resource id
     * @param imageResourceId int: Resource id of the image
     */
    public void setImageFromResource(int imageResourceId) {
        mImageView.setImageResource(imageResourceId);
    }

    /**
     * Set the image to the view
     * @param bitmap Bitmap: The image bitmap
     */
    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    /**
     * Set the Tint of the icon or image in the image view
     * @param color The color resource id
     */
    public void setIconTint(@ColorRes int color) {
        if (mImageView != null)
            mImageView.setColorFilter(ContextCompat.getColor(getContext(), color));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Load the image only if the view is inflated; to prevent app crash
        mImageView = findViewById(R.id.iv_display_picture);
        if (mIsInflationSuccess) {
            if (mDpResourceId != -1) {
                setImageFromResource(mDpResourceId);
            }

            mImageView.setColorFilter(mIconTint); // Icon Tint
            setScaleType(mScaleType);
        }
    }
}