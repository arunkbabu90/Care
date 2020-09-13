package arunkbabu.care.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Constants
import arunkbabu.care.Message
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.adapters.MessageAdapter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity(), ChildEventListener {
    private lateinit var msgRoot: DatabaseReference
    private lateinit var lastMsgRoot: DatabaseReference
    private var adapter: MessageAdapter? = null

    private val messages = ArrayList<Message>()
    private var senderId = ""
    private var receiverId = ""
    private var senderName = ""
    private var senderDpPath = ""

    companion object {
        const val PERSON_NAME_EXTRA_KEY = "key_chat_person_name_extra"
        const val RECEIVER_ID_EXTRA_KEY = "key_chat_receiver_id_extra"
        const val USER_ID_EXTRA_KEY = "key_chat_sender_id_extra"
        const val PROFILE_PICTURE_EXTRA_KEY = "key_chat_profile_picture_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        senderName = intent.getStringExtra(PERSON_NAME_EXTRA_KEY) ?: ""
        senderDpPath = intent.getStringExtra(PROFILE_PICTURE_EXTRA_KEY) ?: ""
        receiverId = intent.getStringExtra(RECEIVER_ID_EXTRA_KEY) ?: ""
        senderId = intent.getStringExtra(USER_ID_EXTRA_KEY) ?: ""

        setSupportActionBar(toolbar_chatActivity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarChat_name.text = senderName
        Utils.loadDpToView(this, senderDpPath, toolbarChat_dp)

        adapter = MessageAdapter(messages, userId = senderId)
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_messages.layoutManager = lm
        rv_messages.adapter = adapter

        lastMsgRoot = Firebase.database.reference.root.child(Constants.ROOT_CHATS).child(senderId)
            .child(receiverId).child(Constants.FIELD_LAST_MESSAGE)
        msgRoot = Firebase.database.reference.child(Constants.ROOT_MESSAGES).child(senderId).child(receiverId)
        msgRoot.orderByChild(Constants.FIELD_MSG_TIMESTAMP).limitToLast(20)
            .addChildEventListener(this)

        toolbarChat_backBtn.setOnClickListener { finish() }
        fab_sendMessage.setOnClickListener {
            val message: String = et_typeMessage.text.toString()
            if (message.isNotBlank()) {
                val msgMap = hashMapOf(
                    Constants.FIELD_MESSAGE to message,
                    Constants.FIELD_SENDER_ID to senderId,
                    Constants.FIELD_RECEIVER_ID to receiverId,
                    Constants.FIELD_MSG_TIMESTAMP to ServerValue.TIMESTAMP
                )
                msgRoot.push().updateChildren(msgMap)
                lastMsgRoot.setValue(message)

                et_typeMessage.setText("")
            }
        }
    }

    /**
     * Loads the message from database to recycler view
     */
    private fun loadMessages(snapshot: DataSnapshot) {
        val message = snapshot.getValue(Message::class.java) ?: Message()
        messages.add(message)

        adapter?.notifyDataSetChanged()
        rv_messages.smoothScrollToPosition(messages.size)
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        loadMessages(snapshot)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

    override fun onChildRemoved(snapshot: DataSnapshot) { }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {  }

    override fun onCancelled(error: DatabaseError) { }
}