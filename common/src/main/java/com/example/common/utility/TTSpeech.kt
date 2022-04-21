package com.example.mynotepad.utility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class TTSpeech(val context: Context) {
    private var tts: TextToSpeech? = null
    private var thread: ThreadA? = null
    var speed = 1.0f
    var pitch = 1.0f

    fun initTTS() {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "TTS Init failed", Toast.LENGTH_SHORT).show()
            }
        }

        thread = ThreadA(tts!!)
    }

    fun close() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }

    fun input(string:String) {
        thread?.input(string)
    }

    fun start() {
        thread?.start()
    }

    fun setPitch() {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate() {
        tts?.setSpeechRate(speed)
    }

    class ThreadA(val tts: TextToSpeech) : Thread() {
        private var string:String = ""

        private var prev = 0
        private var cur = 0
        private var speakContent:String = ""
        override fun run() {
            for (i in 0..string.length) {
                if (string[i] == '.') {
                    cur = i
                    speakContent = string.substring(prev, cur)
                    prev = cur+1
                    ttsSpeak(speakContent)
                    while (tts.isSpeaking) {
                        sleep(1000)
                    }
                }
            }

        }

        fun input(string:String) {
            this.string = string
        }

        private fun ttsSpeak(str: String) {
            tts?.speak(str, TextToSpeech.QUEUE_ADD, null, null)
        }
    }


}