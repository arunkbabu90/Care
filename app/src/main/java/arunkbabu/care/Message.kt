package arunkbabu.care

/**
 * Data Object representing a single Message in the Chat Room
 */
data class Message(val msg: String = "",
                   val senderId: String = "",
                   val msgTimestamp: Long = -1) {

    companion object {
        const val TYPE_YOU = 4000
    }
}