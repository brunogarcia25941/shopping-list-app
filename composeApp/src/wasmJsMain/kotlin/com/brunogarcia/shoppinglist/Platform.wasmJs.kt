package com.brunogarcia.shoppinglist

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import androidx.compose.runtime.Composable
import org.jetbrains.skia.EncodedImageFormat
import androidx.compose.runtime.remember

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}



actual fun getPlatform(): Platform = WasmPlatform()

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}


actual @Composable
fun AdBanner() {
    // Vazio para a Web
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


class WebVibrator : NativeVibrator {
    override fun vibrateHeavy() {
        // A Web não precisa de vibrar, ignoramos
    }
}

@Composable
actual fun rememberNativeVibrator(): NativeVibrator {
    return remember { WebVibrator() }
}


// 1. O código JS tem de estar numa função isolada (top-level) com o sinal de "="
private fun copyToClipboardJs(text: String): Unit = js("""
    {
        navigator.clipboard.writeText(text).then(function() {
            window.alert('Lista copiada com sucesso! / List copied to clipboard!');
        });
    }
""")

// 2. a classe apenas chama essa função!
class WebShareManager : ShareManager {
    override fun shareText(text: String) {
        copyToClipboardJs(text)
    }
}

@Composable
actual fun rememberShareManager(): ShareManager {
    return remember { WebShareManager() }
}

class WebScreenManager : ScreenManager {
    override fun keepScreenOn(keepOn: Boolean) {
        // A Web não suporta (ou não precisa) de impedir o ecrã de apagar desta forma
    }
}

@Composable
actual fun rememberScreenManager(): ScreenManager {
    return remember { WebScreenManager() }
}


class WebWidgetUpdater : WidgetUpdater {
    override fun update() {
        // A Web não tem Widgets de ecrã principal, logo ignoramos
    }
}

@Composable
actual fun rememberWidgetUpdater(): WidgetUpdater {
    return remember { WebWidgetUpdater() }
}

actual val isWidgetSupported: Boolean = false

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    return { onResult(null) }
}


actual fun showRewardedVideo(onRewardEarned: () -> Unit, onAdFailed: () -> Unit) {
    onAdFailed()
}

actual fun savePremiumThemeExpiry(expiryTimestamp: Long) {}
actual fun getPremiumThemeExpiry(): Long = 0L

// --- AS PONTES DIRETAS PARA O JAVASCRIPT ---
@JsFun("() => Date.now()")
external fun jsDateNow(): Double

@JsFun("(url) => window.open(url, '_blank')")
external fun jsWindowOpen(url: String)
// --------------------------------------------------

actual fun getCurrentTimeMillis(): Long {
    // Chama a ponte JS e converte para Long
    return jsDateNow().toLong()
}

actual fun openPlayStore() {
    // Chama a ponte JS para abrir o separador
    jsWindowOpen("https://play.google.com/store/apps/details?id=com.brunogarcia.shoppinglist")
}

actual fun saveGoldIconProgress(videosWatched: Int) {}
actual fun getGoldIconProgress(): Int = 0
actual fun changeAppIcon(isGold: Boolean) {}

