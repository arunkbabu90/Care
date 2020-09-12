package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import arunkbabu.care.R
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private val senderId = ""
    private val receiverId = ""

    companion object {
        const val PERSON_NAME_EXTRA_KEY = "key_chat_person_name_extra"
        const val USER_ID_EXTRA_KEY = "key_chat_user_id_extra"
        const val PROFILE_PICTURE_EXTRA_KEY = "key_chat_profile_picture_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorGreenDark1)
        setSupportActionBar(toolbar_chatActivity)
    }
}