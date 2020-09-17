package arunkbabu.care.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arunkbabu.care.Message
import arunkbabu.care.R
import arunkbabu.care.Utils
import arunkbabu.care.inflate
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.item_message_date.view.*
import kotlinx.android.synthetic.main.item_message_lt.view.*
import kotlinx.android.synthetic.main.item_message_rt.view.*
import java.util.*

class MessageAdapter(
    private val messages: ArrayList<Message>,
    private val userId: String
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Message.TYPE_YOU -> MessageViewHolderRt(
                parent.context,
                parent.inflate(R.layout.item_message_rt)
            )
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
     * Visually Groups the messages by each Day in the chat
     * @param msgTs The current message Timestamp
     * @param futureTs The next message Timestamp
     * @param dtv The text view in which the date should be shown
     * @param dl The included view group item which is holding the #dtv
     * @param sysTs The current timestamp of the system or OS
     */
    private fun groupMsgByDate(msgTs: Long, futureTs: Long,
                               dtv: MaterialTextView, dl: View,
                               sysTs: Long) {
        if (futureTs == 0L) {
            dl.visibility = View.VISIBLE
            dtv.text = Utils.getLogicalDateString(msgTs, sysTs)
        } else {
            val c1: Calendar = Calendar.getInstance(TimeZone.getDefault())
            val c2: Calendar = Calendar.getInstance(TimeZone.getDefault())
            c1.timeInMillis = msgTs
            c2.timeInMillis = futureTs

            val isSameDay = c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                    && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                    && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
            if (isSameDay) {
                dl.visibility = View.GONE
                dtv.text = ""
            } else {
                dl.visibility = View.VISIBLE
                dtv.text = Utils.getLogicalDateString(msgTs, sysTs)
            }
        }
    }

    inner class MessageViewHolderRt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, futureTimestamp: Long) {
            itemView.itemMsgRt_text.text = message.msg

            val time = Utils.getTimeString(context, message.msgTimestamp)
            // Set a tick if message is sent successfully
            if (message.status == Message.STATUS_SEND)
                itemView.itemMsgRt_time.text =  context.getString(R.string.msg_sent, time)
            else
                itemView.itemMsgRt_time.text = time

            groupMsgByDate(message.msgTimestamp, futureTimestamp,
                itemView.tv_itemMsgDate, itemView.itemMsgRt_dateLayout,
                System.currentTimeMillis())
        }
    }

    inner class MessageViewHolderLt(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message, futureTimestamp: Long) {
            itemView.itemMsgLt_text.text = message.msg

            val time = Utils.getTimeString(context, message.msgTimestamp)
            // Set a tick if message is sent successfully
            if (message.status == Message.STATUS_SEND)
                itemView.itemMsgLt_text.text =  context.getString(R.string.msg_sent, time)
            else
                itemView.itemMsgLt_time.text = time
            
            groupMsgByDate(message.msgTimestamp, futureTimestamp,
                itemView.tv_itemMsgDate, itemView.itemMsgLt_dateLayout,
                System.currentTimeMillis())
        }
    }
}