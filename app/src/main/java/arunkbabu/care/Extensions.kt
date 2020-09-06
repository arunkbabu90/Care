package arunkbabu.care

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Extension function to inflate a view using LayoutInflater
 * @param layoutRes int: ID for an XML layout resource to load (e.g., R.layout.main_page)
 * @param attachToRoot boolean: Whether the inflated hierarchy should be attached to the root parameter? If false, root is only used to create the correct subclass of LayoutParams for the root view in the XML.
 * Default: false
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

/**
 * Resize the bitmap keeping the aspect ratio, irrespective of the values
 * @param height The target height
 * @param width The target width
 * @return Bitmap: The scaled down Bitmap
 */
fun Bitmap.resize(height: Int, width: Int): Bitmap {
    val matrix = Matrix()
    matrix.setRectToRect(
        RectF(0f, 0f, this.width.toFloat(), this.height.toFloat()),
        RectF(0f, 0f, height.toFloat(), width.toFloat()), Matrix.ScaleToFit.CENTER)

    return Bitmap.createBitmap(this, 0,0,this.width, this.height, matrix, true)
}
