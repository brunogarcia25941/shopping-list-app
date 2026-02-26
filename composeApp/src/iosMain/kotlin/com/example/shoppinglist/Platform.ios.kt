package com.example.shoppinglist

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual @Composable
fun AdBanner() {
    // Fica vazio no iPhone (para já!)
}