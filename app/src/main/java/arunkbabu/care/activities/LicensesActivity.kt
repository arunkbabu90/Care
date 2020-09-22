package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.R
import kotlinx.android.synthetic.main.activity_licenses.*

class LicensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        setSupportActionBar(tb_licenses)
        // Include "UP" button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb_licenses.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_action_back)

        wv_license.loadUrl("file:///android_asset/Licenses.html")
    }
}