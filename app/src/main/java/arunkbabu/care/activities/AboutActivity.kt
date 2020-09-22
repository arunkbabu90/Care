package arunkbabu.care.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.BuildConfig
import arunkbabu.care.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorDarkBackgroundGrey1)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorDarkBackgroundGrey1)

        tv_about_version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        btn_about_contact.setOnClickListener(this)
        btn_about_licenses.setOnClickListener(this)
        btn_about_privacy_policy.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_about_contact -> {
                // Launch Email App
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_id)))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                }

                if (emailIntent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
                } else {
                    Toast.makeText(applicationContext, getString(R.string.err_no_email), Toast.LENGTH_SHORT).show()
                }
            }

            R.id.btn_about_privacy_policy -> {
                val privacyIntent = Intent(Intent.ACTION_VIEW)
                privacyIntent.data = Uri.parse("https://sites.google.com/view/care-privacypolicy/home")
                startActivity(privacyIntent)
            }

            R.id.btn_about_licenses -> startActivity(Intent(this, LicensesActivity::class.java))
        }
    }
}