package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arunkbabu.care.Constants
import arunkbabu.care.R
import arunkbabu.care.fragments.DocSearchResultsFragment

class SearchResultsActivity : AppCompatActivity() {
    companion object {
        const val KEY_EXTRA_SPECIALITY_ID = "speciality_id_key_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        val specialityId = intent.getIntExtra(KEY_EXTRA_SPECIALITY_ID, Constants.NULL_INT)
        supportFragmentManager.beginTransaction()
            .add(R.id.search_results_container, DocSearchResultsFragment(specialityId))
            .commit()
    }
}