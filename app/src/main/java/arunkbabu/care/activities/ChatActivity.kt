package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arunkbabu.care.R
import arunkbabu.care.Utils
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private var senderId = ""
    private var receiverId = ""
    private var senderName = ""
    private var senderDpPath = ""

    companion object {
        const val PERSON_NAME_EXTRA_KEY = "key_chat_person_name_extra"
        const val USER_ID_EXTRA_KEY = "key_chat_user_id_extra"
        const val PROFILE_PICTURE_EXTRA_KEY = "key_chat_profile_picture_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        senderName = intent.getStringExtra(PERSON_NAME_EXTRA_KEY) ?: ""
        senderDpPath = intent.getStringExtra(PROFILE_PICTURE_EXTRA_KEY) ?: ""
        senderId = intent.getStringExtra(USER_ID_EXTRA_KEY) ?: ""

        setSupportActionBar(toolbar_chatActivity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarChat_name.text = senderName
        Utils.loadDpToView(this, senderDpPath, toolbarChat_dp)

        toolbarChat_backBtn.setOnClickListener { finish() }
    }
}