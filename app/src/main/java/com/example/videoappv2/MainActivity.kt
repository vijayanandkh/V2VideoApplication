package com.example.videoappv2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.videoappv2.ui.theme.VideoAppV2Theme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoAppV2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyVideoApp()
                }
            }
        }
    }
}


@Composable
fun MyVideoApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination ="splash_screen" ) {
        composable("splash_screen") { SplashScreen(navController)}
        composable("main_screen") {

            WebViewScreen(navController ,
                stringResource(R.string.VIDEO_2_URL),
                stringResource(R.string.file_android_res_raw_webview_video_content_2_html))
        }

    }


}

@Composable
fun ExoPlayerView(videoUrl: String, modifier: Modifier = Modifier) {
    val viewModel: VideoPlayerViewModel = viewModel()
    val context = LocalContext.current
    val playerUiState by viewModel.playerUiState.collectAsState()

    val exoPlayer = exoPlayer(context, videoUrl)
    val lister = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            Log.d("MainActivity", "video playing status: $playbackState")

            if(playbackState == ExoPlayer.STATE_ENDED) {
                Log.d("MainActivity", "video playing completed")
                viewModel.updateShowPopupState("show")
                viewModel.performCustomActions("stop")
            }
        }

    }
    exoPlayer.addListener(lister)
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )

    when(playerUiState) {
        is PlayerUiState.Play -> { playVideo(exoPlayer) }
        is PlayerUiState.Pause -> { pauseVideo(exoPlayer) }
        is PlayerUiState.Speed-> { speedPlayVideo(exoPlayer) }
        is PlayerUiState.Stop -> { stopVideo(exoPlayer) }
        PlayerUiState.Loading -> {

        }
    }
    DisposableEffect(Unit) {
        onDispose {
            releasePlayer(exoPlayer)
        }
    }
}

@Composable
private fun exoPlayer(
    context: Context,
    videoUrl: String
): ExoPlayer {

     val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
        }

    }
    return exoPlayer
}

@Composable
fun OverlayWebView(url: String, modifier: Modifier = Modifier) {
    val viewModel : VideoPlayerViewModel = viewModel()
    val showPopupState by viewModel.showPopupUiState.collectAsState()

    val webView = rememberWebViewWithLifecycle()

    AndroidView(
        factory = { context ->
            webView.apply {
                elevation = 12.0f
                addJavascriptInterface(CustomJavaScriptInterface(viewModel), "Android")
                loadUrl(url)
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (newProgress == 100) {
                            view?.evaluateJavascript("closePopup();", null)
                            view?.evaluateJavascript("pausingVideo();", null)
                        }
                    }
                }
            }
        },
        modifier = modifier
    )

    when(showPopupState) {
        "show" -> webView.evaluateJavascript("showPopup();", null)
        "false" -> webView.evaluateJavascript("closePopup();", null)
    }
}


private fun speedPlayVideo(exoPlayer: ExoPlayer) {
    Log.d("MainActivity", "speed action performed")
    val playbackParameters = PlaybackParameters(10.0F)
    exoPlayer.playbackParameters = playbackParameters
}


private fun stopVideo(exoPlayer: ExoPlayer) {
    Log.d("MainActivity", "stop action performed")
    exoPlayer.stop()
    exoPlayer.playWhenReady = false
    exoPlayer.prepare()
    val playbackParameters = PlaybackParameters(1.0F)
    exoPlayer.playbackParameters = playbackParameters
    exoPlayer.seekTo(0)
}

private fun pauseVideo(exoPlayer: ExoPlayer) {
    Log.d("MainActivity", "pause action performed")
    exoPlayer.pause()
    exoPlayer.playWhenReady = false
}

private fun playVideo(exoPlayer: ExoPlayer) {
    Log.d("MainActivity", "play action performed")
    if(exoPlayer.playWhenReady == false)
        exoPlayer.prepare()
    exoPlayer.playWhenReady = true
    exoPlayer.play()
}

fun releasePlayer(exoPlayer: ExoPlayer) {
    exoPlayer.release()
}


@Composable
fun VideoWithWebViewOverlay(videoUrl: String, webViewUrl: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ExoPlayer view as the background
        ExoPlayerView(videoUrl = videoUrl, modifier = Modifier.fillMaxSize())

        // WebView overlay on top of ExoPlayer
        OverlayWebView(
            url = webViewUrl,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}


@Composable
fun WebViewScreen(navController: NavController,videoUrl: String, webViewUrl: String) {
    VideoWithWebViewOverlay(
        videoUrl = videoUrl,
        webViewUrl = webViewUrl)
}

@Composable
 fun SplashScreen( navController: NavController) {

     Box(
         modifier = Modifier.fillMaxSize(),
         contentAlignment = Alignment.Center
     ) {
         Text(
             text = "Welcome to Video application\n\n by Vijayanand",
             fontSize = 30.sp)
     }
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("main_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWebViewScreen() {
    WebViewScreen(
        navController = rememberNavController(),
        stringResource(id = R.string.VIDEO_2_URL),
        stringResource(id = R.string.file_android_res_raw_webview_video_content_1_html)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    SplashScreen(navController = rememberNavController())

}

@Composable
fun rememberWebViewWithLifecycle(): WebView {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
//            alpha = 0.0F
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    return webView
}
