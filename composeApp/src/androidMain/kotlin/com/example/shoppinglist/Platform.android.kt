package com.example.shoppinglist

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.platform.LocalConfiguration

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual @Composable
fun AdBanner() {

    val screenWidth = LocalConfiguration.current.screenWidthDp
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidth))
                // USAR ESTE ID DURANTE O DESENVOLVIMENTO
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                // adUnitId = "ca-app-pub-1817058359358742/2435543601"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}