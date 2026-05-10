package com.numisproerp.ui.splash

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.numisproerp.R

/**
 * Кастомна `VideoView`, яка під час `onMeasure` рівномірно розтягує
 * відео на весь екран (як `centerCrop` у `ImageView`).
 */
class FullScreenVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Use the parent's measured size, ignore intrinsic video size.
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}

/**
 * Compose-обгортка, яка програє відео `R.raw.splash_video`. Викликає
 * [onComplete] коли відео завершилось, було перерване або не змогло завантажитись.
 */
@Composable
fun SplashVideoScreen(onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uri = remember { Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}") }

    // Safety net: завжди завершуємо після 8с, навіть якщо відео не запустилось.
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(8000)
        onComplete()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FullScreenVideoView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.setVolume(0f, 0f) // mute
                        start()
                    }
                    setOnCompletionListener { onComplete() }
                    setOnErrorListener { _, _, _ -> onComplete(); true }
                }
            }
        )
    }
}
