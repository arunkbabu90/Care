package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Chat
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.inflate
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
            itemView.itemChat_name.text = chat.senderName
            itemView.setOnClickListener { itemClickListener(chat) }
        }
    }
}