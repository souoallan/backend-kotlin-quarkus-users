package tech.joupi.users.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.FileInputStream
import java.util.Optional

@ApplicationScoped
class FirebaseInitializer {

    @ConfigProperty(name = "joupi.firebase.service-account-key.path")
    lateinit var serviceAccountPath: Optional<String>

    private val log: Logger = Logger.getLogger(FirebaseInitializer::class.java)

    fun onStart(@Observes ev: StartupEvent?) {
        if (serviceAccountPath.isPresent && serviceAccountPath.get().isNotBlank()) {
            try {
                val serviceAccount = FileInputStream(serviceAccountPath.get())
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                    log.info("Firebase Admin SDK initialized successfully.")
                } else {
                    log.info("Firebase Admin SDK already initialized.")
                }
            } catch (e: Exception) {
                log.error("Error initializing Firebase Admin SDK from path: ${serviceAccountPath.get()}", e)
            }
        } else {
            log.warn("Firebase service account key path ('joupi.firebase.service-account-key.path') is not configured. Firebase Admin SDK not initialized.")
        }
    }
}