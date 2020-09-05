package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arunkbabu.care.R
import arunkbabu.care.fragments.DocSearchResultsFragment
import kotlinx.android.synthetic.main.activity_view_profile.*

class ViewProfileActivity : AppCompatActivity() {
    private var mDocumentId = ""
    private var mFullName = ""
    private var mProfilePicture = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        mDocumentId = intent.getStringExtra(DocSearchResultsFragment.DOCUMENT_ID_EXTRAS_KEY) ?: ""
        mFullName = intent.getStringExtra(DocSearchResultsFragment.DOCTOR_NAME_EXTRAS_KEY) ?: ""
        mProfilePicture = intent.getStringExtra(DocSearchResultsFragment.DOCTOR_DP_EXTRAS_KEY) ?: ""

        tv_viewProfile_error.text = "Document id: $mDocumentId\n\nName: $mFullName\n\nProfile Picture: $mProfilePicture"
    }
}