package com.example.util.english

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class EnglishTTSHelper(context: Context, private val onInitCompleted: (Boolean) -> Unit = {}) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    var isInitialized = false
        private set

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = false
                onInitCompleted(false)
            } else {
                isInitialized = true
                onInitCompleted(true)
            }
        } else {
            isInitialized = false
            onInitCompleted(false)
        }
    }

    fun speak(text: String, isSlow: Boolean = false) {
        if (isInitialized) {
            // Check if string contains Chinese characters
            val containsChinese = text.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
            if (containsChinese) {
                // Force standard Mandarin (Simplified Chinese / mainland China)
                val res = tts?.setLanguage(Locale.CHINA)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                }
            } else {
                tts?.setLanguage(Locale.US)
            }

            val rate = if (isSlow) 0.55f else 0.95f
            tts?.setSpeechRate(rate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun speakWithCompletion(text: String, isSlow: Boolean = false, onDone: () -> Unit) {
        if (!isInitialized || tts == null) {
            onDone()
            return
        }

        val utteranceId = "utterance_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(uId: String?) {}

            override fun onDone(uId: String?) {
                if (uId == utteranceId) {
                    onDone()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(uId: String?) {
                if (uId == utteranceId) {
                    onDone()
                }
            }

            override fun onError(uId: String?, errorCode: Int) {
                if (uId == utteranceId) {
                    onDone()
                }
            }
        })

        val containsChinese = text.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
        if (containsChinese) {
            val res = tts?.setLanguage(Locale.CHINA)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
            }
        } else {
            tts?.setLanguage(Locale.US)
        }

        val rate = if (isSlow) 0.55f else 0.95f
        tts?.setSpeechRate(rate)

        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}


