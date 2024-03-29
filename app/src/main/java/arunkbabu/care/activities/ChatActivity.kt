package arunkbabu.care.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
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

class ChatActivity : AppCompatActivity(), ChildEventListener, View.OnClickListener {
    private lateinit var msgRoot: DatabaseReference
    private lateinit var receiverChatRoot: DatabaseReference
    private lateinit var senderChatRoot: DatabaseReference
    private lateinit var msgQuery: Query
    private var adapter: MessageAdapter? = null

    private val messages = ArrayList<Message>()
    private var senderId = ""
    private var receiverId = ""
    private var receiverName = ""
    private var receiverDpPath = ""
    private var senderName = ""
    private var senderDpPath = ""
    private var receiverUserType: Int = -1

    private var isFirstLaunch = true

    companion object {
        const val RECEIVER_NAME_EXTRA_KEY = "key_chat_receiver_name_extra"
        const val RECEIVER_DP_EXTRA_KEY = "key_chat_receiver_dp_extra"
        const val RECEIVER_ID_EXTRA_KEY = "key_chat_receiver_id_extra"
        const val SENDER_NAME_EXTRA_KEY = "key_chat_sender_name_extra"
        const val SENDER_DP_EXTRA_KEY = "key_chat_sender_dp_extra"
        const val SENDER_ID_EXTRA_KEY = "key_chat_sender_id_extra"
        const val RECEIVER_USER_TYPE_EXTRA_KEY = "key_user_type_receiver_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverUserType = intent.getIntExtra(RECEIVER_USER_TYPE_EXTRA_KEY, -1)
        receiverName = intent.getStringExtra(RECEIVER_NAME_EXTRA_KEY) ?: ""
        receiverDpPath = intent.getStringExtra(RECEIVER_DP_EXTRA_KEY) ?: ""
        receiverId = intent.getStringExtra(RECEIVER_ID_EXTRA_KEY) ?: ""
        senderId = intent.getStringExtra(SENDER_ID_EXTRA_KEY) ?: ""
        senderName = intent.getStringExtra(SENDER_NAME_EXTRA_KEY) ?: ""
        senderDpPath = intent.getStringExtra(SENDER_DP_EXTRA_KEY) ?: ""

        setSupportActionBar(toolbar_chatActivity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (Utils.userType == Constants.USER_TYPE_PATIENT) {
            // User is a patient so chats are doctors; Show a Dr. prefix in names
            toolbarChat_name.text = getString(R.string.doc_name, receiverName)
        } else {
            toolbarChat_name.text = receiverName
        }
        Utils.loadDpToView(this, receiverDpPath, toolbarChat_dp)

        receiverChatRoot = Firebase.database.reference.root.child(Constants.ROOT_CHATS)
            .child(receiverId)
            .child(senderId)

        senderChatRoot = Firebase.database.reference.root.child(Constants.ROOT_CHATS)
            .child(senderId)
            .child(receiverId)

        msgRoot = if (Utils.userType == Constants.USER_TYPE_PATIENT) {
            // User is patient
            Firebase.database.reference.child(Constants.ROOT_MESSAGES)
                .child(senderId)
                .child(receiverId)
        } else {
            // User is doctor
            Firebase.database.reference.child(Constants.ROOT_MESSAGES)
                .child(receiverId)
                .child(senderId)
        }
        loadMessages()

        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        lm.stackFromEnd = true
        adapter = MessageAdapter(messages, userId = senderId)
        rv_messages.layoutManager = lm
        rv_messages.adapter = adapter

        rv_messages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, prevBottom ->
            // Scroll to the end of list when keyboard pop
            if (bottom < prevBottom)
                rv_messages.smoothScrollToPosition(messages.size)
        }

        toolbarChat_backBtn.setOnClickListener(this)
        toolbarChat_name.setOnClickListener(this)
        fab_sendMessage.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.toolbarChat_backBtn -> {
                finish()
            }
            R.id.fab_sendMessage -> {
                val message: String = et_typeMessage.text.toString()
                val newMsgRoot = msgRoot.push()
                val newMsgKey = newMsgRoot.key
                if (message.isNotBlank()) {
                    val msgMap = hashMapOf(
                        Constants.FIELD_MESSAGE to message,
                        Constants.FIELD_SENDER_ID to senderId,
                        Constants.FIELD_RECEIVER_ID to receiverId,
                        Constants.FIELD_MSG_TIMESTAMP to ServerValue.TIMESTAMP
                    )
                    newMsgRoot.updateChildren(msgMap)
                    // Also push your id to doctor's chat index in "Chats". So that the receiver can
                    // connect with you
                    val sChatMap = hashMapOf(
                        Constants.FIELD_FULL_NAME to senderName,
                        Constants.FIELD_PROFILE_PICTURE to senderDpPath,
                        Constants.FIELD_CHAT_TIMESTAMP to ServerValue.TIMESTAMP,
                        Constants.FIELD_LAST_MESSAGE to message
                    )
                    val rChatMap = hashMapOf(
                        Constants.FIELD_FULL_NAME to receiverName,
                        Constants.FIELD_PROFILE_PICTURE to receiverDpPath,
                        Constants.FIELD_CHAT_TIMESTAMP to ServerValue.TIMESTAMP,
                        Constants.FIELD_LAST_MESSAGE to message
                    )
                    receiverChatRoot.updateChildren(sChatMap)
                    senderChatRoot.updateChildren(rChatMap)

                    et_typeMessage.setText("")
                }
            }
            R.id.toolbarChat_name -> {
                val vpIntent = Intent(this, ViewProfileActivity::class.java)
                vpIntent.putExtra(ViewProfileActivity.USER_ID_EXTRAS_KEY, receiverId)
                vpIntent.putExtra(ViewProfileActivity.NAME_EXTRAS_KEY, receiverName)
                vpIntent.putExtra(ViewProfileActivity.DP_EXTRAS_KEY, receiverDpPath)
                vpIntent.putExtra(ViewProfileActivity.USER_TYPE_EXTRAS_KEY, receiverUserType)
                vpIntent.putExtra(ViewProfileActivity.IS_VIEW_MODE_EXTRAS_KEY, true)
                startActivity(vpIntent)
            }
        }
    }

    /**
     * Loads the messages
     */
    private fun loadMessages() {
        msgQuery = msgRoot.orderByChild(Constants.FIELD_MSG_TIMESTAMP)
        msgQuery.addChildEventListener(this)
    }

    /**
     * Loads the message from database to recycler view
     */
    private fun updateDataItems(snapshot: DataSnapshot) {
        val message = snapshot.getValue(Message::class.java) ?: Message()
        message.key = snapshot.key ?: ""
        messages.add(message)

        if (isFirstLaunch) {
            Utils.runPullDownAnimation(this, rv_messages)
            isFirstLaunch = false
        }

        rv_messages?.smoothScrollToPosition(messages.size)
        adapter?.notifyDataSetChanged()
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        updateDataItems(snapshot)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }
    override fun onChildRemoved(snapshot: DataSnapshot) { }
    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {  }
    override fun onCancelled(error: DatabaseError) { }
}