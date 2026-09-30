package com.velmora.vic.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.velmora.vic.R
import com.velmora.vic.skills.PhoneSkillManager
import com.velmora.vic.voice.VicConversationEngine
import com.velmora.vic.voice.VicSpeechManager
import com.velmora.vic.voice.VicTextToSpeechManager
import com.velmora.vic.wolf.WolfWebView
import kotlinx.coroutines.launch

class AssistActivity : AppCompatActivity(),
    VicSpeechManager.SpeechEventsListener,
    VicTextToSpeechManager.TtsEventsListener {

    companion object {
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 101
    }

    private lateinit var wolfWebView: WolfWebView
    private lateinit var tvStateBadge: TextView
    private lateinit var tvUserQuery: TextView
    private lateinit var tvVicResponse: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var fabMic: FloatingActionButton
    private lateinit var cardConfirmation: CardView
    private lateinit var tvConfirmTitle: TextView
    private lateinit var tvConfirmDetails: TextView
    private lateinit var btnApproveAction: Button
    private lateinit var btnRejectAction: Button

    private lateinit var speechManager: VicSpeechManager
    private lateinit var ttsManager: VicTextToSpeechManager
    private lateinit var conversationEngine: VicConversationEngine

    private var pendingWhatsAppDraft: PhoneSkillManager.WhatsAppDraft? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assist)

        initViews()
        initServices()
        checkPermissionsAndListen()
    }

    private fun initViews() {
        wolfWebView = findViewById(R.id.wolfWebView)
        tvStateBadge = findViewById(R.id.tvStateBadge)
        tvUserQuery = findViewById(R.id.tvUserQuery)
        tvVicResponse = findViewById(R.id.tvVicResponse)
        btnClose = findViewById(R.id.btnClose)
        fabMic = findViewById(R.id.fabMic)
        cardConfirmation = findViewById(R.id.cardConfirmation)
        tvConfirmTitle = findViewById(R.id.tvConfirmTitle)
        tvConfirmDetails = findViewById(R.id.tvConfirmDetails)
        btnApproveAction = findViewById(R.id.btnApproveAction)
        btnRejectAction = findViewById(R.id.btnRejectAction)

        btnClose.setOnClickListener {
            finish()
        }

        fabMic.setOnClickListener {
            startListeningSequence()
        }

        btnApproveAction.setOnClickListener {
            pendingWhatsAppDraft?.let { draft ->
                val ok = PhoneSkillManager.executeWhatsAppDispatch(this, draft.recipient, draft.message)
                if (ok) {
                    tvVicResponse.text = "Dispatched draft to WhatsApp."
                    wolfWebView.setWolfState("success")
                } else {
                    tvVicResponse.text = "Failed to launch WhatsApp."
                    wolfWebView.setWolfState("error")
                }
            }
            cardConfirmation.visibility = View.GONE
            pendingWhatsAppDraft = null
        }

        btnRejectAction.setOnClickListener {
            cardConfirmation.visibility = View.GONE
            pendingWhatsAppDraft = null
            tvVicResponse.text = "Operation cancelled."
            wolfWebView.setWolfState("idle")
            tvStateBadge.text = "IDLE"
        }
    }

    private fun initServices() {
        speechManager = VicSpeechManager(this, this)
        ttsManager = VicTextToSpeechManager(this, this)
        conversationEngine = VicConversationEngine(this)
    }

    private fun checkPermissionsAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListeningSequence()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun startListeningSequence() {
        ttsManager.stop()
        cardConfirmation.visibility = View.GONE
        tvStateBadge.text = "LISTENING"
        wolfWebView.setWolfState("listening")
        tvUserQuery.text = "Listening…"
        speechManager.startListening()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListeningSequence()
        } else {
            tvUserQuery.text = "Microphone permission required."
            tvStateBadge.text = "ERROR"
            wolfWebView.setWolfState("error")
        }
    }

    // --- SpeechEventsListener Callbacks ---

    override fun onSpeechListeningStarted() {
        tvStateBadge.text = "LISTENING"
        wolfWebView.setWolfState("listening")
    }

    override fun onSpeechRmsChanged(normalizedRms: Float) {
        wolfWebView.setVoiceLevel(normalizedRms)
    }

    override fun onSpeechRecognized(text: String, isFinal: Boolean) {
        tvUserQuery.text = text
        if (isFinal) {
            processVoiceQuery(text)
        }
    }

    override fun onSpeechError(errorMsg: String) {
        tvStateBadge.text = "STANDBY"
        wolfWebView.setWolfState("idle")
        tvUserQuery.text = errorMsg
    }

    private fun processVoiceQuery(query: String) {
        tvStateBadge.text = "THINKING"
        wolfWebView.setWolfState("thinking")

        lifecycleScope.launch {
            val response = conversationEngine.processQuery(query)

            tvVicResponse.text = response.spokenResponse
            tvStateBadge.text = if (response.wolfState == "success") "SUCCESS" else "RESPONDING"
            wolfWebView.setWolfState(response.wolfState)

            // Check if confirmation gate is triggered
            if (response.requiresUserConfirmation && response.actionType == VicConversationEngine.ActionType.WHATSAPP_CONFIRM) {
                val draft = response.payload as? PhoneSkillManager.WhatsAppDraft
                if (draft != null) {
                    pendingWhatsAppDraft = draft
                    tvConfirmTitle.text = "Confirm WhatsApp Message"
                    tvConfirmDetails.text = "To: ${draft.recipient}\nMessage: \"${draft.message}\""
                    cardConfirmation.visibility = View.VISIBLE
                }
            }

            // Speak natural response
            ttsManager.speak(response.spokenResponse)
        }
    }

    // --- TtsEventsListener Callbacks ---

    override fun onTtsStarted() {
        runOnUiThread {
            wolfWebView.setWolfState("speaking")
            wolfWebView.setVoiceLevel(0.8f)
        }
    }

    override fun onTtsCompleted() {
        runOnUiThread {
            wolfWebView.setVoiceLevel(0f)
            if (pendingWhatsAppDraft == null) {
                wolfWebView.setWolfState("idle")
                tvStateBadge.text = "IDLE"
            }
        }
    }

    override fun onTtsError(error: String) {
        runOnUiThread {
            wolfWebView.setWolfState("error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
