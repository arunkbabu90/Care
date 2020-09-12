package arunkbabu.care.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import arunkbabu.care.Chat
import arunkbabu.care.R
import arunkbabu.care.activities.ChatActivity
import arunkbabu.care.adapters.ChatAdapter
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_messages.*

class MessagesFragment : Fragment() {
    private lateinit var adapter: ChatAdapter
    private var chats = ArrayList<Chat>()
    private var userId = ""

    companion object {
        var messagesFragmentActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messagesFragmentActive = true

        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        adapter =  ChatAdapter(chats) { chat -> startChatActivity(chat) }
        lm.stackFromEnd = true
        rv_messagesFrag.layoutManager = lm
        rv_messagesFrag.adapter = adapter
    }

    private fun startChatActivity(chat: Chat) {
        val i = Intent(context, ChatActivity::class.java)
        i.putExtra(ChatActivity.PERSON_NAME_EXTRA_KEY, chat.senderName)
        i.putExtra(ChatActivity.PROFILE_PICTURE_EXTRA_KEY, chat.profilePicture)
        i.putExtra(ChatActivity.USER_ID_EXTRA_KEY, id)
        startActivity(i)
    }

    /**
     * Updates the recycler views with the new data
     * @param chats The list of new chats
     */
    fun updateData(chats: ArrayList<Chat>) {
        this.chats = chats
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesFragmentActive = false
    }
}