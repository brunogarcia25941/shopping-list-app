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
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size).asImageBitmap()
}

actual fun ByteArray.compressImage(): ByteArray {
    return try {
        // 1. Descobre o tamanho original sem sobrecarregar a memória
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(this, 0, this.size, options)

        // 2. Calcula a redução (Se for gigante, divide o tamanho por 2, 4, 8, etc.)
        var scale = 1
        while (options.outWidth / scale / 2 >= 800 && options.outHeight / scale / 2 >= 800) {
            scale *= 2
        }

        // 3. Carrega a imagem já miniatura para a memória
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size, decodeOptions) ?: return this

        // 4. Comprime a qualidade para JPEG (60% de qualidade)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        this // Se algo correr mal (ex: ficheiro corrompido), devolve a original
    }
}

class AndroidVibrator(private val context: Context) : NativeVibrator {
    override fun vibrateHeavy() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // AQUI ESTÁ O TEU CONTROLO!
            // 50 = tempo em milissegundos (rápido e seco)
            // 255 = Força máxima do motor (vai de 1 a 255)
            vibrator.vibrate(VibrationEffect.createOneShot(50, 255))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
}

@Composable
actual fun rememberNativeVibrator(): NativeVibrator {
    val context = LocalContext.current
    return remember { AndroidVibrator(context) }
}