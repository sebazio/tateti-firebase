package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.actividad_inicial.*
import java.lang.Exception
import java.util.*
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.messaging.ktx.messaging


class ActividadInicial : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)

        //CONOCER TOKEN PARA FCM
        /*
        Firebase.messaging.token.addOnCompleteListener {
            if(it.isSuccessful) {
                Log.d("Tateti", it.result!!)
            }
        }
        */


        val deviceLanguage = Locale.getDefault().displayLanguage
        Firebase.analytics.logEvent("Pantalla inicial Cargada") {
            param("deviceLanguage", deviceLanguage)
        }
        FirebaseCrashlytics.getInstance().setCustomKey("deviceLanguage", deviceLanguage)

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        if (usuarioEstaLogueado()) {
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }

        actualizarRemoteConfig()


    }

    private fun usuarioEstaLogueado(): Boolean {
        //OK TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
        if (Firebase.auth.currentUser != null) {
            return true
        }
        return false
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        Firebase.analytics.logEvent("Boton de registrarse presionado") { }
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 5
            fetchTimeoutInSeconds = 10
        }
        Firebase.remoteConfig.setConfigSettingsAsync(settings)

        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        //OK TODO-04-REMOTECONFIG
        // Configurar los valores por default para remote config,
        // ya sea por codigo o por XML
        val default = mapOf(
                "forgetPassButton" to true
        )
        Firebase.remoteConfig.setDefaultsAsync(default)
    }

    private fun configurarOlvideMiContrasena() {
        //OK TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña
        Firebase.remoteConfig.fetchAndActivate()
                .addOnCompleteListener {

                    val forgetPassButton = Firebase.remoteConfig.getBoolean("forgetPassButton")
                    val botonOlvideHabilitado = forgetPassButton
                            if (botonOlvideHabilitado) {
                                olvideMiContrasena.visibility = View.VISIBLE
                            } else {
                                olvideMiContrasena.visibility = View.GONE
                            }
                }
    }

    private fun olvideMiContrasena() {
        Firebase.analytics.logEvent("Boton de Olvide Mi Contraseña presionado") { }
        // Obtengo el mail
        val email = email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            //OK TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion
            Firebase.auth.sendPasswordResetEmail(email)
              .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                      Snackbar.make(rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
                  } else {
                      Snackbar.make(rootView, "Error " + task.exception, Snackbar.LENGTH_SHORT).show()
                  }
              }
        }
    }

    private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
        if (task.isSuccessful) {
            FirebaseCrashlytics.getInstance().setUserId(Firebase.auth.currentUser!!.uid)
            Firebase.analytics.setUserId(Firebase.auth.currentUser!!.uid)
            if (usuarioVerificoEmail()) {
                verPartidas()
            } else {
                desloguearse()
                Snackbar.make(rootView!!, "Verifica tu email para continuar", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            if (task.exception is FirebaseAuthInvalidUserException) {
                Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
            } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesion() {
        Firebase.analytics.logEvent("Boton de iniciar sesión presionado") { }
        FirebaseCrashlytics.getInstance().log("get email")
        val email = email.text.toString()
        FirebaseCrashlytics.getInstance().log("get password")
        val password = password.text.toString()

        try {
            if (email == "" || password == "") {
                throw IllegalArgumentException("Login tried without mail or password")
            } else {
                //OK TODO-05-AUTHENTICATION
                // IMPORTANTE: Eliminar  la siguiente linea cuando se implemente authentication
                //verPartidas()
                //OK TODO-05-AUTHENTICATION
                // hacer signInWithEmailAndPassword con los valores ingresados de email y password
                // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo
                Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, authenticationListener)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }



    private fun usuarioVerificoEmail(): Boolean {
        //OK TODO-05-AUTHENTICATION
        // Preguntar al currentUser si verifico email
        return Firebase.auth.currentUser!!.isEmailVerified
    }

    private fun desloguearse() {
        //OK TODO-05-AUTHENTICATION
        // Hacer signOut de Firebase
        Firebase.auth.signOut()
    }
}