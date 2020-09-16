package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Message
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.inflate
import kotlinx.android.synthetic.main.item_message_date.view.*
import kotlinx.android.synthetic.main.item_message_lt.view.*
import kotlinx.android.synthetic.main.item_message_rt.view.*

class MessageAdapter(private val messages: ArrayList<Message>,
                     private val userId: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isFirstRun = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Message.TYPE_YOU -> MessageViewHolderRt(parent.context, parent.inflate(R.layout.item_message_rt))
            else -> MessageViewHolderLt(parent.context, parent.inflate(R.layout.item_message_lt))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg: Message = messages[position]
        var futureTimestamp: Long = -1
        if (position != 0) {
            // Look only up until 0th position; otherwise position - 1 will throw an OutOfBounds Exception
            futureTimestamp = messages[position - 1].msgTimestamp
        }

        if (msg.senderId == userId) {
            (holder as MessageViewHolderRt).bind(msg, futureTimestamp)
        } else {
            (holder as MessageViewHolderLt).bind(msg, futureTimestamp)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val uid: String = messages[position].senderId
        if (uid == userId)
            return Message.TYPE_YOU

        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Checks whether the full Long date can be added to list
     * @param timestamp The timestamp of the current message
     * @param futureTimestamp The timestamp of the next message
     */
    private fun isDateAddable(timestamp: Long, futureTimestamp: Long): Boolean {
        val day = Utils.getDay(timestamp)
        val previousDay = Utils.getDay(futureTimestamp)

        return previousDay < day

//        val day = Integer.parseInt(Utils.getDayString(timestamp))
//        if (day < previousDay)
//            isAlreadyAdded = false
//
//        return if (isFirstRun || (day < previousDay && !isAlreadyAdded)) {
//            previousDay = day
//            isAlreadyAdded = true
//            isFirstRun = false
//            true
//        } else {
//            false
//        }
    }

    inner class MessageViewHolderRt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, futureTimestamp: Long) {
            itemView.itemMsgRt_text.text = message.msg
            itemView.itemMsgRt_time.text = Utils.getTimeString(context, message.msgTimestamp)
            if (isDateAddable(message.msgTimestamp, futureTimestamp)) {
                itemView.itemMsgRt_dateLayout.visibility = View.VISIBLE
                itemView.tv_itemMsgDate.text = Utils.getLogicalDateString(message.msgTimestamp, System.currentTimeMillis())
            }
        }
    }

    inner class MessageViewHolderLt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, futureTimestamp: Long) {
            itemView.itemMsgLt_text.text = message.msg
            itemView.itemMsgLt_time.text = Utils.getTimeString(context, message.msgTimestamp)
            if (isDateAddable(message.msgTimestamp, futureTimestamp)) {
                itemView.itemMsgLt_dateLayout.visibility = View.VISIBLE
                itemView.tv_itemMsgDate.text = Utils.getLogicalDateString(message.msgTimestamp, System.currentTimeMillis())
            }
        }
    }
}