package com.example.shoppinglist

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
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