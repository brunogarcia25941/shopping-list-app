package com.brunogarcia.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. Prepara o pedido de consentimento
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)

        // 2. Pergunta à Google: "Este utilizador precisa de ver o pop-up europeu?"
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                // 3. Se precisar (estiver na Europa e for a 1ª vez), mostra o pop-up!
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { error ->
                    if (error != null) {
                        println("Erro ao mostrar pop-up: ${error.message}")
                    }

                    // Se o utilizador aceitou agora, inicializa os anúncios.
                    if (consentInformation.canRequestAds()) {
                        MobileAds.initialize(this) {}
                    }
                }
            },
            { error ->
                println("Falha ao pedir info do RGPD: ${error.message}")
            }
        )

        // Se a pessoa já tinha aceite ontem, não esperamos
        // pelo código de cima e inicializamos os anúncios instantaneamente
        if (consentInformation.canRequestAds()) {
            MobileAds.initialize(this) {}
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}