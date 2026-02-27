package com.example.shoppinglist

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


class WebShareManager : ShareManager {
    override fun shareText(text: String) {
        // Usa JavaScript nativo para copiar para o Clipboard e dar um aviso
        js("""
            navigator.clipboard.writeText(text).then(function() {
                alert('Lista copiada com sucesso! / List copied to clipboard!');
            });
        """)
    }
}

@Composable
actual fun rememberShareManager(): ShareManager {
    return remember { WebShareManager() }
}