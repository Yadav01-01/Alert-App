package com.alert.app.fragment.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.SwipeAdapter
import com.alert.app.databinding.FragmentMessageBinding
import com.alert.app.model.message.ChatListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var swipeAdapter: SwipeAdapter

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.setImageShowTv()?.visibility = View.GONE
        mainActivity.setImgChatBoot().visibility = View.GONE

        swipeAdapter = SwipeAdapter(requireContext())

        binding.rcyData.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = swipeAdapter
        }

        loadChatList()

        binding.imgNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }

        binding.threeLine.setOnClickListener {
            with(mainActivity.getDrawerLayout()) {
                if (isDrawerVisible(GravityCompat.START)) {
                    closeDrawer(GravityCompat.START)
                } else {
                    openDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun loadChatList() {
        if (currentUserId.isEmpty()) return
        Log.d("CHAT_DEBUG", "currentUserId = $currentUserId")

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val participants = doc.get("participants") as? List<*> ?: return@forEach
                    val otherUserId = participants.firstOrNull { it != currentUserId } ?: return@forEach

                    // Fetch other user info
                    db.collection("users").document(otherUserId.toString()).get()
                        .addOnSuccessListener { userDoc ->
                            val otherUserName = userDoc.getString("name") ?: "User"
                            val otherUserImage = userDoc.getString("profileImage") ?: ""

                            val chatItem = ChatListItem(
                                chatId = doc.id,
                                otherUserId = otherUserId.toString(),
                                lastMessage = doc.getString("lastMessage") ?: "",
                                lastMessageTime = doc.getLong("lastMessageTime") ?: 0,
                                unreadCount = doc.getLong("unread_$currentUserId")?.toInt() ?: 0,
                                otherUserName = otherUserName,
                                otherUserImage = otherUserImage
                            )

                            //  Update adapter
                            swipeAdapter.updateOrAdd(chatItem)
                        }
                }
            }
    }

}
