package ar.com.develup.tateti

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

class TaTeTiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //OK TODO-07-NOTIFICATION
        // Suscribirse al topic "general"
        FirebaseMessaging.getInstance().subscribeToTopic("general")
    }
}