package arunkbabu.care.activities

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import arunkbabu.care.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_view_picture.*
import kotlin.math.max
import kotlin.math.min

class ViewPictureActivity : AppCompatActivity(),  GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private lateinit var mScaleDetector: ScaleGestureDetector
    private lateinit var mGestureDetector: GestureDetectorCompat
    private var mActivePointerId: Int = MotionEvent.INVALID_POINTER_ID
    private var mScaleFactor: Float = 1.0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f

    companion object {
        val PROFILE_PICTURE_PATH_EXTRA_KEY = "key_profile_picture_path_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_picture)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val imagePath = intent.getStringExtra(PROFILE_PICTURE_PATH_EXTRA_KEY)
        Glide.with(this).load(imagePath).into(iv_viewPicture)

        mGestureDetector = GestureDetectorCompat(this, this)
        mGestureDetector.setOnDoubleTapListener(this)
        mScaleDetector = ScaleGestureDetector(this, ScaleGestureListener())
    }

    /**
     * Resets the ImageView to its original size or scales it up
     * @param event The MotionEvent object
     */
    private fun resetOrZoom(event: MotionEvent?) {
        if (mScaleFactor > 1.0f || mScaleFactor < 1.0f) {
            // Reset
            mLastTouchX = 0f
            mLastTouchY = 0f
            mPosX = 0f
            mPosY = 0f
            mActivePointerId = MotionEvent.INVALID_POINTER_ID
            mScaleFactor = 1.0f
        } else {
            // Zoom
            mLastTouchX = 0f
            mLastTouchY = 0f
            mPosX = 0f
            mPosY = 0f
            mActivePointerId = event?.getPointerId(0) ?: -1
            mScaleFactor = 2.0f
            mScaleFactor = max(0.1f, min(mScaleFactor, 10.0f))
        }
        invalidate()
    }

    /**
     * Applies the current position and scale to the ImageView
     */
    private fun invalidate() {
        iv_viewPicture.scaleX = mScaleFactor
        iv_viewPicture.scaleY = mScaleFactor
        iv_viewPicture.x = mPosX
        iv_viewPicture.y = mPosY
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mScaleDetector.onTouchEvent(event)
        mGestureDetector.onTouchEvent(event)

        when (event?.action ?: -1 and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = event?.actionIndex ?: 0
                // Remember where we started dragging
                mLastTouchX = event?.getX(pointerIndex) ?: 0f
                mLastTouchY = event?.getY(pointerIndex) ?: 0f
                // Save the ID of the pointer for dragging
                mActivePointerId = event?.getPointerId(0) ?: -1
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event?.findPointerIndex(mActivePointerId) ?: 0
                val x = event?.getX(pointerIndex) ?: 0f
                val y = event?.getY(pointerIndex) ?: 0f

                // Calculate the distance moved
                val dx = x - mLastTouchX
                val dy = y - mLastTouchY

                mPosX += dx
                mPosY += dy

                invalidate()

                // Cache touch position for next zoom so that when zooming again ,it
                // won't reset its position
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event?.actionIndex ?: 0
                val pointerId = event?.getPointerId(pointerIndex) ?: 0

                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = event?.getX(newPointerIndex) ?: 0f
                    mLastTouchY = event?.getY(newPointerIndex) ?: 0f
                    mActivePointerId = event?.getPointerId(newPointerIndex) ?: -1
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
        return true
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures
     */
    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            mScaleFactor *= detector?.scaleFactor ?: 1.0f
            mScaleFactor = max(0.1f, min(mScaleFactor, 10.0f))
            iv_viewPicture.scaleX = mScaleFactor
            iv_viewPicture.scaleY = mScaleFactor
            return true
        }
    }

    override fun onDown(p0: MotionEvent?): Boolean = false
    override fun onShowPress(p0: MotionEvent?) {}
    override fun onLongPress(p0: MotionEvent?) { }
    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false
    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false
    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false
    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean = false
    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean = false
    override fun onDoubleTap(p0: MotionEvent?): Boolean {
        resetOrZoom(p0)
        return true
    }
}