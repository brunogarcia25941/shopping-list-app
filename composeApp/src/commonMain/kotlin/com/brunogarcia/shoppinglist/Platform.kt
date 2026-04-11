package com.brunogarcia.shoppinglist

import androidx.compose.runtime.Composable


interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// ECRÃ SEMPRE LIGADO (Wakelock)
interface ScreenManager {
    fun keepScreenOn(keepOn: Boolean)
}

@Composable
expect fun rememberScreenManager(): ScreenManager

// ATUALIZADOR DE WIDGETS
interface WidgetUpdater {
    fun update()
}

@Composable
expect fun rememberWidgetUpdater(): WidgetUpdater

// Diz-nos se esta plataforma suporta o nosso widget de ecrã principal
expect val isWidgetSupported: Boolean

// LANÇADOR DA CÂMARA
@Composable
expect fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): () -> Unit

// O Motor de Anúncios Premiados
expect fun showRewardedVideo(onRewardEarned: () -> Unit, onAdFailed: () -> Unit)

// A Memória do Sistema
expect fun savePremiumThemeExpiry(expiryTimestamp: Long)
expect fun getPremiumThemeExpiry(): Long

// Pergunta que horas são em milissegundos
expect fun getCurrentTimeMillis(): Long

// O Cofre dos Ícones (Conta quantos vídeos já foram vistos)
expect fun saveGoldIconProgress(videosWatched: Int)
expect fun getGoldIconProgress(): Int

// A função que muda fisicamente o ícone no telemóvel
expect fun changeAppIcon(isGold: Boolean)

// Abre a página da app na loja de aplicações
expect fun openPlayStore()