package com.example.billbudddy

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {
    private val appId = "5af99c0456534a7bad51b2bc92c0f201"
    private var channelName = ""
    private lateinit var receiverName: String
    private var rtcEngine: RtcEngine? = null
    
    private var localVideoView: FrameLayout? = null
    private var remoteVideoView: FrameLayout? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private var isMuted = false
    private var isVideoEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        
        channelName = intent.getStringExtra("channelName") ?: return finish()
        receiverName = intent.getStringExtra("receiverName") ?: "User"
        
        supportActionBar?.title = "Call with $receiverName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initUI()
        
        try {
            initializeAndJoinChannel()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing video call: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initUI() {
        localVideoView = findViewById(R.id.local_video_view_container)
        remoteVideoView = findViewById(R.id.remote_video_view_container)

        findViewById<ImageButton>(R.id.audioButton).setOnClickListener { toggleAudio() }
        findViewById<ImageButton>(R.id.videoButton).setOnClickListener { toggleVideo() }
        findViewById<ImageButton>(R.id.endCallButton).setOnClickListener { endCall() }
        findViewById<ImageButton>(R.id.switchCameraButton).setOnClickListener { switchCamera() }
    }

    private fun initializeAndJoinChannel() {
        rtcEngine = RtcEngine.create(baseContext, appId, object : IRtcEngineEventHandler() {
            override fun onUserJoined(uid: Int, elapsed: Int) {
                runOnUiThread { setupRemoteVideo(uid) }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                runOnUiThread { removeRemoteVideo() }
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                runOnUiThread {
                    Toast.makeText(baseContext, "Joined Channel Successfully", Toast.LENGTH_SHORT).show()
                }
            }
        })


        rtcEngine?.apply {
            enableVideo()
            enableAudio()
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        }

        setupLocalVideo()


        rtcEngine?.joinChannel(null, channelName, null, 0)
    }

    private fun setupLocalVideo() {

        localSurfaceView = SurfaceView(baseContext).apply {
            setZOrderMediaOverlay(true)
        }
        localVideoView?.addView(localSurfaceView)
        

        rtcEngine?.setupLocalVideo(VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }

    private fun setupRemoteVideo(uid: Int) {
        if (remoteSurfaceView == null) {
            remoteSurfaceView = SurfaceView(baseContext)
            remoteVideoView?.addView(remoteSurfaceView)
            rtcEngine?.setupRemoteVideo(VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            remoteSurfaceView?.visibility = View.VISIBLE
        }
    }

    private fun removeRemoteVideo() {
        remoteSurfaceView?.visibility = View.GONE
        remoteVideoView?.removeAllViews()
        remoteSurfaceView = null
    }

    private fun toggleAudio() {
        isMuted = !isMuted
        rtcEngine?.muteLocalAudioStream(isMuted)
        findViewById<ImageButton>(R.id.audioButton).setImageResource(
            if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
        )
    }

    private fun toggleVideo() {
        isVideoEnabled = !isVideoEnabled
        rtcEngine?.muteLocalVideoStream(!isVideoEnabled)
        findViewById<ImageButton>(R.id.videoButton).setImageResource(
            if (isVideoEnabled) R.drawable.ic_videocam else R.drawable.ic_videocam_off
        )
        localVideoView?.visibility = if (isVideoEnabled) View.VISIBLE else View.INVISIBLE
    }

    private fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    private fun endCall() {
        leaveChannel()
        finish()
    }

    private fun leaveChannel() {
        rtcEngine?.apply {
            stopPreview()
            leaveChannel()
        }
        

        remoteVideoView?.removeAllViews()
        localVideoView?.removeAllViews()
        remoteSurfaceView = null
        localSurfaceView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        endCall()
        super.onBackPressed()
    }
} 