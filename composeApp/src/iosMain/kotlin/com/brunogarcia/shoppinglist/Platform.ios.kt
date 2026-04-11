package com.brunogarcia.shoppinglist

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval


class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual @Composable
fun AdBanner() {
    // Fica vazio no iPhone (para já!)
}

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}



actual fun ByteArray.compressImage(): ByteArray {
    return try {
        val image = Image.makeFromEncoded(this)
        // Transforma a foto num JPEG leve, com apenas 30% da qualidade original
        // Como é para uma app de compras, não precisamos de 4K!
        val data = image.encodeToData(EncodedImageFormat.JPEG, 30)
        data?.bytes ?: this
    } catch (e: Exception) {
        this
    }
}

class IosVibrator : NativeVibrator {
    override fun vibrateHeavy() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        generator.impactOccurred()
    }
}

@Composable
actual fun rememberNativeVibrator(): NativeVibrator {
    return remember { IosVibrator() }
}

class IosShareManager : ShareManager {
    override fun shareText(text: String) {
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController

        if (rootViewController != null) {
            val activityVC = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            rootViewController.presentViewController(activityVC, animated = true, completion = null)
        }
    }
}

@Composable
actual fun rememberShareManager(): ShareManager {
    return remember { IosShareManager() }
}

class IosScreenManager : ScreenManager {
    override fun keepScreenOn(keepOn: Boolean) {
        UIApplication.sharedApplication.idleTimerDisabled = keepOn
    }
}

@Composable
actual fun rememberScreenManager(): ScreenManager {
    return remember { IosScreenManager() }
}

class IosWidgetUpdater : WidgetUpdater {
    override fun update() {
        // No iOS usa-se o WidgetKit (fica para uma próxima atualização)
    }
}

@Composable
actual fun rememberWidgetUpdater(): WidgetUpdater {
    return remember { IosWidgetUpdater() }
}

actual val isWidgetSupported: Boolean = false

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    return { onResult(null) } // No iOS, para já, não faz nada
}



actual fun showRewardedVideo(onRewardEarned: () -> Unit, onAdFailed: () -> Unit) {
    // Como não temos AdMob no iOS configurado, dá sempre falha
    onAdFailed()
}

actual fun savePremiumThemeExpiry(expiryTimestamp: Long) { /* Por fazer no iOS */ }
actual fun getPremiumThemeExpiry(): Long = 0L

// No iOS, as horas calculam-se de maneira diferente
@OptIn(ExperimentalForeignApi::class)
actual fun getCurrentTimeMillis(): Long {
    // Falamos diretamente com o relógio do processador do iPhone
    return memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L)
    }
}

actual fun saveGoldIconProgress(videosWatched: Int) { /* Por fazer no iOS */ }
actual fun getGoldIconProgress(): Int = 0
actual fun changeAppIcon(isGold: Boolean) { /* Por fazer no iOS */ }

actual fun openPlayStore() {
    // No futuro mudar para o link da App Store da Apple
    val url = NSURL(string = "https://apps.apple.com/")
    // O !! é obrigatório aqui porque o NSURL pode ser nulo
    UIApplication.sharedApplication.openURL(url!!)
}