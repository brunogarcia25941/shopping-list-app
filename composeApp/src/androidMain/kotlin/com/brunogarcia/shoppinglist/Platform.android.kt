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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.lang.ref.WeakReference
import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.ActivityNotFoundException


// Uma variável global SEGURA que não causa fugas de memória
var MainContext: WeakReference<Activity>? = null




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
                // adUnitId = "ca-app-pub-3940256099942544/6300978111"
                adUnitId = "ca-app-pub-1817058359358742/2435543601"
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

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    val context = LocalContext.current
    var tempFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempFile != null) {
            // Lê a foto em ALTA QUALIDADE
            val bytes = tempFile!!.readBytes()
            onResult(bytes)
            tempFile!!.delete() // Apaga o ficheiro temporário para não ocupar memória ao telemóvel
        } else {
            onResult(null)
            tempFile?.delete()
        }
    }

    return {
        // 1. Cria a pasta "images" dentro da cache
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        // 2. Cria um ficheiro em branco
        val file = File.createTempFile("foto_supermercado_", ".jpg", imagesDir)
        tempFile = file

        // 3. Pede o "URI Seguro" ao FileProvider para entregar à Câmara da Google/Samsung
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // 4. Lança a Câmara
        launcher.launch(uri)
    }
}

// -----------------
// ANÚNCIO PARA TEMA PREMIUM
// -----------------

// Variável global para guardar o anúncio carregado em segundo plano
private var mRewardedAd: RewardedAd? = null

// Função para pré-carregar o vídeo (chamar isto na MainActivity)
fun preloadRewardedVideo(context: Context) {
    val adRequest = AdRequest.Builder().build()
    // Este é o ID de TESTE da Google para vídeos premiados.
    // Depois trocar pelo ID Real do AdMob!
    RewardedAd.load(context, "ca-app-pub-1817058359358742/8314574099", adRequest, object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
            mRewardedAd = ad
        }
    })
}

// Implementação do expect para MOSTRAR o anúncio
actual fun showRewardedVideo(onRewardEarned: () -> Unit, onAdFailed: () -> Unit) {
    val activity = MainContext?.get() ?: return // Precisamos de guardar o contexto (passo abaixo)

    if (mRewardedAd != null) {
        mRewardedAd?.show(activity as Activity) { rewardItem ->
            // O UTILIZADOR VIU O VÍDEO ATÉ AO FIM
            onRewardEarned()
            // Carrega o próximo vídeo para estar pronto
            preloadRewardedVideo(activity)
        }
    } else {
        onAdFailed()
        preloadRewardedVideo(activity) // Tenta carregar outra vez
    }
}

// Implementação da Memória (SharedPreferences)
actual fun savePremiumThemeExpiry(expiryTimestamp: Long) {
    val context = MainContext?.get() ?: return
    val prefs = context.getSharedPreferences("PremiumPrefs", Context.MODE_PRIVATE)
    prefs.edit().putLong("theme_expiry", expiryTimestamp).apply()
}

actual fun getPremiumThemeExpiry(): Long {
    val context = MainContext?.get() ?: return 0L
    val prefs = context.getSharedPreferences("PremiumPrefs", Context.MODE_PRIVATE)
    return prefs.getLong("theme_expiry", 0L)
}

// O Android responde com o tempo do sistema ao Platform.kt
actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}


// --------------
// ICONE DESBLOQUEADO POR VIDEO
// --------------

actual fun saveGoldIconProgress(videosWatched: Int) {
    val context = MainContext?.get() ?: return
    context.getSharedPreferences("PremiumPrefs", Context.MODE_PRIVATE)
        .edit().putInt("gold_icon_progress", videosWatched).apply()
}

actual fun getGoldIconProgress(): Int {
    val context = MainContext?.get() ?: return 0
    return context.getSharedPreferences("PremiumPrefs", Context.MODE_PRIVATE)
        .getInt("gold_icon_progress", 0)
}

actual fun changeAppIcon(isGold: Boolean) {
    val context = MainContext?.get() ?: return
    val pm = context.packageManager

    // O nome da atividade normal e o nome do "disfarce" Dourado
    val defaultComponent = ComponentName(context, "com.brunogarcia.shoppinglist.MainActivity")
    val goldComponent = ComponentName(context, "com.brunogarcia.shoppinglist.MainActivityGold")

    if (isGold) {
        // Desliga o ícone normal e liga o dourado
        pm.setComponentEnabledSetting(defaultComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(goldComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    } else {
        // Liga o normal e desliga o dourado
        pm.setComponentEnabledSetting(goldComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(defaultComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }
}

actual fun openPlayStore() {
    val context = MainContext?.get() ?: return
    val packageName = context.packageName

    try {
        // Tenta abrir diretamente na aplicação da Play Store
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Se a pessoa não tiver a Play Store instalada, abre no navegador de internet
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}