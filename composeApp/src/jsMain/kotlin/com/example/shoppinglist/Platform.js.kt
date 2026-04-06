package com.brunogarcia.shoppinglist

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    return { onResult(null) }
}