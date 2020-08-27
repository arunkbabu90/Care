package arunkbabu.care.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import arunkbabu.care.R;

public class DrawableImageView extends AppCompatImageView implements View.OnTouchListener {
    private float mUpx = 0;
    private float mUpy = 0;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Context mContext;

    public boolean mIsModified;

    private final int TOUCH_DOT_TRANSPARENCY = 0x70;     // Give 70% Transparency to the Touch Dot

    public DrawableImageView(Context context) {
        super(context);
        mContext = context;
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOnTouchListener(this);
    }

    /**
     * Set an image to this view
     * @param alteredBitmap The overlay bitmap for applying modifications
     * @param bmp The actual image for modification
     */
    public void setNewImage(Bitmap alteredBitmap, Bitmap bmp) {
        mCanvas = new Canvas(alteredBitmap);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(TOUCH_DOT_TRANSPARENCY);

        Paint imgPaint = new Paint();

        mCanvas.drawBitmap(bmp, 0,0, imgPaint);

        setImageBitmap(alteredBitmap);
        mBitmap = alteredBitmap;
        mIsModified = false;
    }

    /**
     * Returns the bitmap that is shown in the View. Any changes like touches or tints will be
     * reflected in the image
     * @return The modified bitmap
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * Tints the supplied image with Red color omitting the background
     * @param alteredBitmap The overlay bitmap to apply tint
     * @param bmp The actual image to apply the red filter
     */
    public void applyFullBody(Bitmap alteredBitmap, Bitmap bmp) {
        mCanvas = new Canvas(alteredBitmap);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColorFilter(new LightingColorFilter(ContextCompat.getColor(mContext, R.color.colorFullBody), Color.BLACK));

        mCanvas.drawBitmap(bmp, 0, 0, mPaint);

        setImageBitmap(alteredBitmap);
        mBitmap = alteredBitmap;
        mIsModified = true;
    }

    /**
     * Tints the supplied image with Red color omitting the background & without displaying
     * the resulting image in this view
     * @param alteredBitmap The overlay bitmap to apply tint
     * @param bmp The actual image to apply the red filter
     * @return Bitmap  The tinted bitmap
     */
    public Bitmap applyFullBodyQuietly(Bitmap alteredBitmap, Bitmap bmp) {
        Canvas canvas = new Canvas(alteredBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(new LightingColorFilter(ContextCompat.getColor(mContext, R.color.colorFullBody), Color.BLACK));
        canvas.drawBitmap(bmp, 0, 0, mPaint);
        mIsModified = true;

        return alteredBitmap;
    }

    /**
     * Returns whether the supplied image has some touch dots or is having a tint or have any
     * modifications
     * @return True if modified; False otherwise
     */
    public boolean getIsModified() {
        return mIsModified;
    }

    /**
     * Setter method for getIsModified()
     * @param isModified Whether the image has modifications
     */
    public void setIsModified(boolean isModified) {
        mIsModified = isModified;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int radius = 50; // touch point size

        switch (action) {
            case MotionEvent.ACTION_UP:
                mUpx = getPointerCoordinates(event)[0]; //event.getX();
                mUpy = getPointerCoordinates(event)[1]; //event.getY();
                if (mUpx > 0 && mUpx < mBitmap.getWidth()) {
                    int color = mBitmap.getPixel((int) mUpx, (int) mUpy);
                    // Avoid drawing touch point over background
                    if (color != Color.TRANSPARENT) {
                        mCanvas.drawCircle(mUpx, mUpy, radius, mPaint);
                        mIsModified = true;
                        invalidate();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private float[] getPointerCoordinates(MotionEvent e) {
        final int index = e.getActionIndex();
        final float[] coordinates = new float[] { e.getX(index), e.getY(index) };

        Matrix matrix = new Matrix();
        getImageMatrix().invert(matrix);
        matrix.postTranslate(getScrollX(), getScrollY());
        matrix.mapPoints(coordinates);
        return coordinates;
    }
}