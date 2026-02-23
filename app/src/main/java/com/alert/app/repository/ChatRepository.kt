package com.alert.app.repository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import android.location.Location
import com.alert.app.model.ChatUserModel
import com.alert.app.model.Message
import com.alert.app.model.MessageType
import com.alert.app.model.message.ChatListItem
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.filter
import kotlin.compareTo

@Singleton
class ChatRepository @Inject constructor(private val firestore: FirebaseFirestore,
                                         private val locationClient: FusedLocationProviderClient
) {
//old delete for me
//    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
//        val listener = firestore.collection("chats")
//            .document(chatId)
//            .collection("messages")
//            .orderBy("timestamp")
//
//            .addSnapshotListener { snapshot, _ ->
//                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
//                trySend(messages)
//            }
//
//        awaitClose { listener.remove() }
//    }




    fun observeMessages(
        chatId: String,
        currentUserId: String
    ): Flow<List<Message>> = callbackFlow {

        if (chatId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val chatRef = firestore.collection("chats").document(chatId)
        val messagesRef = chatRef.collection("messages")
            .orderBy("timestamp")

        // delete-for-me time (milliseconds)
        var deleteTime = 0L

        // 🔥 Observe delete-for-me changes
        val chatListener = chatRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val deletedAtMap =
                snapshot?.get("deletedAt") as? Map<String, Long> ?: emptyMap()

            deleteTime = deletedAtMap[currentUserId] ?: 0L
        }

        // 🔥 Observe messages
        val messageListener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val visibleMessages = snapshot?.documents
                ?.mapNotNull { it.toObject(Message::class.java) }
                ?.filter { message ->
                    val messageTime =
                        message.timestamp?.toDate()?.time ?: 0L
                    messageTime > deleteTime
                }
                ?: emptyList()

            trySend(visibleMessages)
        }

        awaitClose {
            chatListener.remove()
            messageListener.remove()
        }
    }


    // 🔹 SEND TEXT MESSAGE (FIXED)
    suspend fun sendTextMessage(
        chatId: String,
        text: String,
        senderId: String,
        receiverId: String
    ) {

        val chatRef = firestore.collection("chats").document(chatId)

        val messageRef = chatRef.collection("messages").document()

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            type = MessageType.TEXT,
            timestamp = Timestamp.now()
        )

        firestore.runBatch { batch ->
            batch.set(messageRef, message)
            batch.set(
                chatRef,
                mapOf(
                    "participants" to listOf(senderId, receiverId),
                    "lastMessage" to text,
                    "lastMessageTime" to message.timestamp,
                    "lastSenderId" to senderId,
                    "unreadCount_$receiverId" to FieldValue.increment(1)
                ),
                SetOptions.merge()
            )
        }.await()


    }

    fun deleteChatForMe(
        chatId: String,
        currentUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        val chatRef = firestore.collection("chats").document(chatId)

        chatRef.update("deletedAt.$currentUserId", System.currentTimeMillis())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                // document/field not exist → create map safely
                val data = mapOf("deletedAt" to mapOf(currentUserId to System.currentTimeMillis()))
                chatRef.set(data, SetOptions.merge())
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { e2 ->
                        e2.printStackTrace()
                        onResult(false)
                    }
            }
    }


//    fun observeChatList(
//        users: List<ChatUserModel>, myUserId: String
//    ): Flow<List<ChatListItem>> = callbackFlow {
//
//        val chatIds = users.mapNotNull { it.chat_id }
//
//        if (chatIds.isEmpty()) {
//            trySend(emptyList())
//            close()
//            return@callbackFlow
//        }
//
//        val listener = firestore.collection("chats")
//            .whereIn(FieldPath.documentId(), chatIds)
//            .addSnapshotListener { snapshot, error ->
//
//                if (error != null || snapshot == null) {
//                    trySend(emptyList())
//                    return@addSnapshotListener
//                }
//                val chatMap = snapshot.documents.associateBy { it.id }
//                val result = users.mapNotNull { user ->
//                val chatDoc = chatMap[user.chat_id] ?: return@mapNotNull null
//                    val isLiveLocation = chatDoc.getString("type") == "location"
//                    ChatListItem(
//                        chatId = user.chat_id!!,
//                        userId = user.id!!,
//                        fullName = user.full_name.orEmpty(),
//                        profile = user.profile.orEmpty(),
//                        lastMessage = chatDoc.getString("lastMessage"),
//                        lastMessageTime = chatDoc.getTimestamp("lastMessageTime"), // ✅ FIX
//                        unreadCount = chatDoc
//                            .getLong("unreadCount_$myUserId")
//                            ?.toInt() ?: 0,
//                        isLiveLocation = isLiveLocation
//                    )
//                }.sortedByDescending { it.lastMessageTime?.toDate()?.time ?: 0 }
//
//                trySend(result)
//            }
//        awaitClose { listener.remove() }
//    }

    fun observeChatList(
        users: List<ChatUserModel>,
        myUserId: String
    ): Flow<List<ChatListItem>> = callbackFlow {

        val chatIds = users.mapNotNull { it.chat_id }

        if (chatIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("chats")
            .whereIn(FieldPath.documentId(), chatIds)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val result = snapshot.documents.mapNotNull { chatDoc ->

                    val chatId = chatDoc.id
                    val user = users.find { it.chat_id == chatId } ?: return@mapNotNull null

                    // 🔥 delete-for-me time
                    val deletedAtMap =
                        chatDoc.get("deletedAt") as? Map<String, Long> ?: emptyMap()

                    val deleteTime = deletedAtMap[myUserId] ?: 0L

                    val lastMessageTime =
                        chatDoc.getTimestamp("lastMessageTime")?.toDate()?.time ?: 0L

                    // ❌ Agar last message delete time se pehle ka hai → chat mat dikhao
                    if (lastMessageTime <= deleteTime) {
                        return@mapNotNull null
                    }

                    val isLiveLocation = chatDoc.getString("type") == "location"

                    ChatListItem(
                        chatId = chatId,
                        userId = user.id!!,
                        fullName = user.full_name.orEmpty(),
                        profile = user.profile.orEmpty(),
                        lastMessage = chatDoc.getString("lastMessage"),
                        lastMessageTime = chatDoc.getTimestamp("lastMessageTime"),
                        unreadCount = chatDoc
                            .getLong("unreadCount_$myUserId")
                            ?.toInt() ?: 0,
                        isLiveLocation = isLiveLocation
                    )
                }.sortedByDescending {
                    it.lastMessageTime?.toDate()?.time ?: 0
                }

                trySend(result)
            }

        awaitClose { listener.remove() }
    }

        suspend fun markChatRead(chatId: String, myUserId: String) {
        firestore.collection("chats")
            .document(chatId)
            .set(
                mapOf("unreadCount_$myUserId" to 0),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun startLiveLocation(
        chatId: String,
        senderId: String,
        receiverId: String,
        initialLocation: GeoPoint
    ): String {
        val docRef = firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document()

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            type = MessageType.LOCATION,
            location = initialLocation,
            isLive = true,
            timestamp = Timestamp.now()
        )

        docRef.set(message).await()
        return docRef.id // 🔥 save this messageId
    }

    suspend fun updateLiveLocation(
        chatId: String,
        messageId: String,
        newLocation: GeoPoint
    ) {
        firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "location" to newLocation,
                    "timestamp" to Timestamp.now()
                )
            )
            .await()
    }

    suspend fun stopLiveLocation(
        chatId: String,
        messageId: String
    ) {
        firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "isLive" to false,
                    "timestamp" to Timestamp.now()
                )
            )
            .await()
    }


    suspend fun createLiveLocationMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        duration: Int
    ): String {

        val expiresAt = Timestamp(
            System.currentTimeMillis() / 1000 + duration * 60,
            0
        )

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            type = MessageType.LOCATION, // marks as location
            isLive = true,
            expiresAt = expiresAt,
            timestamp = Timestamp.now()
        )

        val messageRef = firestore.collection("chats").document(chatId)
            .collection("messages")
            .document()

        firestore.runBatch { batch ->
            // Save the live location message
            batch.set(messageRef, message)

            // Update chat document's lastMessageTime and lastMessage
            batch.set(
                firestore.collection("chats").document(chatId),
                mapOf(
                    "lastMessage" to "Live location", // text for chat list
                    "lastMessageTime" to message.timestamp,
                    "lastSenderId" to senderId,
                    "unreadCount_$receiverId" to FieldValue.increment(1)
                ),
                SetOptions.merge()
            )
        }.await()

        return messageRef.id
    }



 }


    // 🔹 SEND LIVE LOCATION (FIXED)
//    suspend fun sendLiveLocation(
//        chatId: String,
//        senderId: String,
//        receiverId: String
//    ) {
//        val location = locationClient.lastLocation.await() ?: return
//
//        val message = Message(
//            senderId = senderId,
//            receiverId = receiverId,
//            type = MessageType.LOCATION,
//            location = GeoPoint(location.latitude, location.longitude),
//            isLive = true,
//            timestamp = Timestamp.now()
//        )
//
//        val chatRef = firestore.collection("chats").document(chatId)
//        val messageRef = chatRef.collection("messages").document()
//
//        firestore.runBatch { batch ->
//            batch.set(messageRef, message)
//
//            batch.set(
//                chatRef,
//                mapOf(
//                    "participants" to listOf(senderId, receiverId),
//                    "lastMessage" to "📍 Live location",
//                    "lastMessageTime" to message.timestamp,
//                    "lastSenderId" to senderId,
//                    "unreadCount_$receiverId" to FieldValue.increment(1)
//                ),
//                SetOptions.merge()
//            )
//        }.await()
//    }
//
//    // 🔹 MARK CHAT READ (NO CRASH)



