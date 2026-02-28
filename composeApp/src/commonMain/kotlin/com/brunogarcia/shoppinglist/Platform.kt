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