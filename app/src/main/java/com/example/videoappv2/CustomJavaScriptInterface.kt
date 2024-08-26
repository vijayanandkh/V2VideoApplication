package com.example.videoappv2

import android.webkit.JavascriptInterface
import androidx.lifecycle.ViewModel

class CustomJavaScriptInterface(private val viewModel: VideoPlayerViewModel) {

    @JavascriptInterface
    fun performAction(action : String) {
        viewModel.performCustomActions(action)
    }
}