package arunkbabu.care

/**
 * Data Object representing a single Message in the Chat Room
 */
data class Message(var key: String = "",
                   val msg: String = "",
                   val senderId: String = "",
                   val receiverId: String = "",
                   val status: Int = -1,
                   val msgTimestamp: Long = -1) {

    companion object {
        const val TYPE_YOU = 4000
        const val STATUS_SEND = 1
    }
}