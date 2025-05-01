package com.furia.furiafanapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.auth.FirebaseAuth
import com.furia.furiafanapp.utils.ShopDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FuriaFanApp : Application() {
    
    @Inject
    lateinit var shopDataInitializer: ShopDataInitializer
    
    override fun onCreate() {
        super.onCreate()
        // Disable reCAPTCHA verification for email/password auth (testing)
        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        
        // Inicializar dados da loja
        CoroutineScope(Dispatchers.IO).launch {
            shopDataInitializer.initializeShopData()
        }
    }
}