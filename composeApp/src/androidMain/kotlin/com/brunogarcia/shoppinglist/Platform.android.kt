package com.brunogarcia.shoppinglist

import android.annotation.SuppressLint
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
import android.content.Intent
import android.app.Activity
import android.view.WindowManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // 50 = tempo em milissegundos (rápido e seco)
            // 255 = Força máxima do motor (vai de 1 a 255)
            vibrator.vibrate(VibrationEffect.createOneShot(30, 200))
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

class AndroidShareManager(private val context: Context) : ShareManager {
    override fun shareText(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Partilhar Lista")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}

@Composable
actual fun rememberShareManager(): ShareManager {
    val context = LocalContext.current
    return remember { AndroidShareManager(context) }
}

class AndroidScreenManager(private val activity: Activity?) : ScreenManager {
    override fun keepScreenOn(keepOn: Boolean) {
        if (keepOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
actual fun rememberScreenManager(): ScreenManager {
    // O Android Studio pode reclamar do cast as? Activity, mas podes ignorar ou aceitar o @Suppress("UNCHECKED_CAST")
    val activity = LocalContext.current as? Activity
    return remember { AndroidScreenManager(activity) }
}


class AndroidWidgetUpdater(private val context: Context) : WidgetUpdater {
    override fun update() {
        val appContext = context.applicationContext
        // O Android obriga que a atualização do Widget seja feita de forma "invisível" (background)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ShoppingWidget().updateAll(appContext)
            } catch (e: Exception) {
                println("Erro ao atualizar widget: ${e.message}")
            }
        }
    }
}

@Composable
actual fun rememberWidgetUpdater(): WidgetUpdater {
    val context = LocalContext.current
    return remember { AndroidWidgetUpdater(context) }
}

actual val isWidgetSupported: Boolean = true