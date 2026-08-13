package com.example.util

import android.content.Context
import com.example.util.english.EnglishTTSHelper

object SpeechSynthesizer {
    private var helper: EnglishTTSHelper? = null

    fun speak(context: Context, text: String, isSlow: Boolean = false) {
        val currentHelper = helper ?: EnglishTTSHelper(context.applicationContext) {
            helper?.speak(text, isSlow)
        }.also { helper = it }

        if (currentHelper.isInitialized) {
            currentHelper.speak(text, isSlow)
        }
    }

    fun stop() {
        helper?.stop()
    }
}
