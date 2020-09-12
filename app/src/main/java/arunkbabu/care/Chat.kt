package arunkbabu.care

/**
 * Data Object representing a single Chat Person in the Messages Fragment list
 */
data class Chat(val senderName: String = "",
                val profilePicture: String = "",
                val chatTimestamp: Long = -1)