package arunkbabu.care

/**
 * Data Object representing a single Chat Person in the Messages Fragment list
 */
data class Chat(var key: String = "",
                val full_name: String = "",
                val profilePicture: String = "",
                val lastMessage: String = "",
                val chatTimestamp: Long = -1 )