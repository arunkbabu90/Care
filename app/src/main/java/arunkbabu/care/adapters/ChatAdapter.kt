package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.*
import kotlinx.android.synthetic.main.item_chats.view.*

class ChatAdapter(private val chats: ArrayList<Chat>,
                  private val itemClickListener: (Chat) -> Unit)
    : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = parent.inflate(R.layout.item_chats)
        return ChatViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chat = chats[position], itemClickListener)
    }

    override fun getItemCount(): Int = chats.size

    class ChatViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        fun bind(chat: Chat, itemClickListener: (Chat) -> Unit) {
            if (chat.profilePicture.isNotBlank()) {
                Utils.loadDpToView(context, chat.profilePicture, itemView.itemChat_dp)
            }
            if (Utils.userType == Constants.USER_TYPE_PATIENT) {
                // User is a patient so chats are doctors; Show a Dr. prefix in names
                itemView.itemChat_name.text = context.getString(R.string.doc_name, chat.full_name)
            } else {
                itemView.itemChat_name.text = chat.full_name
            }
            itemView.itemChat_lastMsg.text = if (chat.lastMessage.isBlank()) "No Messages" else chat.lastMessage
            itemView.itemChat_date.text = Utils.getLogicalShortDate(chat.chatTimestamp)
            itemView.setOnClickListener { itemClickListener(chat) }
        }
    }
}