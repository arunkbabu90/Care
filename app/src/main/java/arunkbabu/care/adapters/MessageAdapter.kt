package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Message
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.inflate
import kotlinx.android.synthetic.main.item_message_lt.view.*
import kotlinx.android.synthetic.main.item_message_rt.view.*

class MessageAdapter(private val messages: ArrayList<Message>,
                     private val userId: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Message.TYPE_YOU -> MessageViewHolderRt(parent.context, parent.inflate(R.layout.item_message_rt))
            else -> MessageViewHolderLt(parent.context, parent.inflate(R.layout.item_message_lt))
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

    class MessageViewHolderRt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            itemView.itemMsgRt_text.text = message.msg
            itemView.itemMsgRt_time.text = Utils.getTimeString(context, message.msgTimestamp)
        }
    }

    class MessageViewHolderLt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            itemView.itemMsgLt_text.text = message.msg
            itemView.itemMsgLt_time.text = Utils.getTimeString(context, message.msgTimestamp)
        }
    }
}