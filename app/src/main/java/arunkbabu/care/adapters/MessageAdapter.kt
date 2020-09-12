package arunkbabu.care.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Message
import arunkbabu.care.R
import arunkbabu.care.inflate
import kotlinx.android.synthetic.main.item_message_lt.view.*
import kotlinx.android.synthetic.main.item_message_rt.view.*

class MessageAdapter(private val messages: ArrayList<Message>,
                     private val userId: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Message.TYPE_YOU -> MessageViewHolderRt(parent.inflate(R.layout.item_message_rt))
            else -> MessageViewHolderLt(parent.inflate(R.layout.item_message_lt))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg: Message = messages[position]
        if (msg.senderId == userId)
            (holder as MessageViewHolderRt).bind(msg)
        else
            (holder as MessageViewHolderLt).bind(msg)
    }

    override fun getItemViewType(position: Int): Int {
        val uid: String = messages[position].senderId
        if (uid == userId) {
            return Message.TYPE_YOU
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolderRt(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            itemView.itemMsgRt_text.text = message.msg
        }
    }

    class MessageViewHolderLt(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            itemView.itemMsgLt_text.text = message.msg
        }
    }
}