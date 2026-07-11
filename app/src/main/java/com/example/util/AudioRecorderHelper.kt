package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var currentFilePath: String? = null
        private set

    fun startRecording(fileName: String): Boolean {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val audioDir = File(dir, "recitations").apply { mkdirs() }
        val file = File(audioDir, fileName)
        currentFilePath = file.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentFilePath)
                prepare()
                start()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
    }

    fun startPlaying(onCompletion: () -> Unit) {
        val path = currentFilePath
        if (path == null) {
            onCompletion()
            return
        }
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(path)
                setOnCompletionListener {
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    onCompletion()
                    true
                }
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                onCompletion()
            }
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    fun deleteCurrentRecording() {
        currentFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        currentFilePath = null
    }

    fun release() {
        stopRecording()
        stopPlaying()
    }
}
