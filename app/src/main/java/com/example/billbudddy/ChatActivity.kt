package com.example.billbudddy

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.billbudddy.Adapter.ChatAdapter
import com.example.billbudddy.Domain.ChatMessage
import com.example.billbudddy.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class ChatActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PERMISSION_REQUEST_CODE = 123
        private const val STORAGE_PERMISSION_CODE = 2
        private const val TAG = "ChatActivity"
    }
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: StorageReference
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var receiverId: String
    private lateinit var receiverName: String
    private lateinit var chatId: String
    private var isCloudinaryInitialized = false
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        receiverId = intent.getStringExtra("receiverId") ?: run {
            Toast.makeText(this, "Error: No receiver ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        receiverName = intent.getStringExtra("receiverName") ?: "Chat"


        chatId = createChatId(auth.currentUser!!.uid, receiverId)
        

        databaseReference = FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(chatId)
            .child("messages")

        storage = FirebaseStorage.getInstance().reference
        binding.toolbarTitle.text = receiverName

        setupRecyclerView()
        setupSendButton()
        loadMessages()
        updateChatList()


        setupImageButton()
        setupVideoCallButton()

    }

    private fun createChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages, auth.currentUser?.uid ?: "")
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser ?: return
        val messageId = databaseReference.push().key ?: return
        
        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "Sender",
            receiverId = receiverId,
            message = messageText,
            messageType = "text",
            timestamp = System.currentTimeMillis(),
            chatId = chatId
        )

        databaseReference.child(messageId).setValue(message)
            .addOnSuccessListener {
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                updateChatList()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateChatList() {

        val chatListRef = FirebaseDatabase.getInstance().reference.child("chatList")
        val lastMessage = messages.lastOrNull()
        
        if (lastMessage != null) {
            val chatInfo = mapOf(
                "lastMessage" to lastMessage.message,
                "timestamp" to lastMessage.timestamp,
                "unreadCount" to 0
            )


            chatListRef.child(auth.currentUser!!.uid)
                .child(receiverId)
                .updateChildren(chatInfo)


            chatListRef.child(receiverId)
                .child(auth.currentUser!!.uid)
                .updateChildren(chatInfo)
        }
    }

    private fun loadMessages() {
        databaseReference.orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    try {
                        val message = snapshot.getValue(ChatMessage::class.java)
                        if (message != null) {
                            messages.add(message)
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message", e)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun pickImage() {
        Log.d(TAG, "pickImage called")
        if (checkAndRequestStoragePermissions()) {
            Log.d(TAG, "Storage permissions granted")
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            try {
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
                Log.d(TAG, "Image picker launched")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching image picker", e)
                Toast.makeText(this, "Error launching image picker: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Storage permissions not granted")
        }
    }

    private fun checkAndRequestStoragePermissions(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_CODE)
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=${data?.data}")
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data?.data != null) {
            uploadImage(data.data!!)
        }
    }

    private fun setupImageButton() {
        binding.attachImageButton.setOnClickListener {
            pickImage()
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val progressDialog = ProgressDialog(this).apply {
            setTitle("Uploading Image...")
            setCancelable(false)
            show()
        }

        try {
            MediaManager.get().upload(imageUri)
                .unsigned("chat_image")
                .option("resource_type", "auto")
                .option("folder", "billbuddy_chat")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Start uploading image to Cloudinary")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100) / totalBytes
                        progressDialog.setMessage("Uploaded $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        progressDialog.dismiss()
                        val imageUrl = resultData["secure_url"] as String
                        Log.d(TAG, "Upload successful. Image URL: $imageUrl")
                        sendImageMessage(imageUrl)
                        Toast.makeText(this@ChatActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        progressDialog.dismiss()
                        Toast.makeText(this@ChatActivity, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        progressDialog.dismiss()
                        Toast.makeText(this@ChatActivity, "Upload rescheduled", Toast.LENGTH_SHORT).show()
                    }
                }).dispatch()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e(TAG, "Error in uploadImage", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendImageMessage(imageUrl: String) {
        val currentUser = auth.currentUser ?: return
        val messageId = databaseReference.push().key ?: return
        
        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUser.uid,
            senderName = currentUser.displayName ?: "User",
            receiverId = receiverId,
            message = imageUrl,
            messageType = "image",
            timestamp = System.currentTimeMillis(),
            chatId = chatId
        )

        databaseReference.child(messageId).setValue(message)
            .addOnSuccessListener {
                Log.d(TAG, "Image message sent successfully")
                updateChatList()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending image message", e)
                Toast.makeText(this, "Failed to send image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupVideoCallButton() {
        binding.videoCallButton.setOnClickListener {
            if (checkSelfPermission()) {
                startVideoCall()
            }
        }
    }

    private fun checkSelfPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
            return false
        }
        return true
    }

    private fun startVideoCall() {
        val intent = Intent(this, VideoCallActivity::class.java).apply {
            putExtra("channelName", chatId)
            putExtra("receiverName", receiverName)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startVideoCall()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
} 