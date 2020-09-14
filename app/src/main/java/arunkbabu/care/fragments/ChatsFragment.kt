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
import arunkbabu.care.activities.DoctorActivity
import arunkbabu.care.activities.PatientActivity
import arunkbabu.care.adapters.ChatAdapter
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment() {
    private lateinit var adapter: ChatAdapter
    private var chats = ArrayList<Chat>()
    private var senderId = ""
    private var senderName = ""
    private var senderDpPath = ""

    companion object {
        var isFragmentActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pa: PatientActivity?
        val da: DoctorActivity?

        pa = try {
            activity as PatientActivity
        } catch (e: ClassCastException) {
            e.printStackTrace()
            null
        }

        da = try {
            activity as DoctorActivity
        } catch (e: ClassCastException) {
            e.printStackTrace()
            null
        }

        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        lm.stackFromEnd = true
        adapter =  ChatAdapter(chats) { chat -> startChatActivity(chat) }
        rv_messagesFrag.layoutManager = lm
        rv_messagesFrag.adapter = adapter

        if (pa != null) {
            updateData(pa.chats)
            senderId = pa.userId
            senderName = pa.fullName
            senderDpPath = pa.patientDpPath
        }

        if (da != null) {
            updateData(da.chats)
            senderId = da.userId
            senderName = da.doctorFullName
            senderDpPath = da.doctorDpPath
        }

        isFragmentActive = true
    }

    private fun startChatActivity(chat: Chat) {
        val i = Intent(context, ChatActivity::class.java)
        i.putExtra(ChatActivity.RECEIVER_NAME_EXTRA_KEY, chat.full_name)
        i.putExtra(ChatActivity.RECEIVER_DP_EXTRA_KEY, chat.profilePicture)
        i.putExtra(ChatActivity.RECEIVER_ID_EXTRA_KEY, chat.key)
        i.putExtra(ChatActivity.SENDER_NAME_EXTRA_KEY, senderName)
        i.putExtra(ChatActivity.SENDER_DP_EXTRA_KEY, senderDpPath)
        i.putExtra(ChatActivity.SENDER_ID_EXTRA_KEY, senderId)
        startActivity(i)
    }

    /**
     * Updates the recycler views with the new data
     * @param chats The list of new chats
     */
    fun updateData(chats: ArrayList<Chat>) {
        this.chats.clear()
        this.chats.addAll(chats)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFragmentActive = false
    }
}