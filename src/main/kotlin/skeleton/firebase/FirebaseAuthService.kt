package skeleton.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.google.firebase.auth.UserRecord.CreateRequest
import com.google.firebase.auth.UserRecord.UpdateRequest
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import skeleton.exception.UserAlreadyExistsException
import skeleton.exception.UserCreationException
import java.util.Random

@ApplicationScoped
class FirebaseAuthService {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    fun createUser(email: String, password: String?, displayName: String? = null): UserRecord {
        val request = CreateRequest()
            .setEmail(email)
            .setPassword(password ?: generateRandomPassword())

        displayName?.let { request.setDisplayName(it) }

        try {
            return firebaseAuth.createUser(request)
        } catch (e: FirebaseAuthException) {
            if (e.authErrorCode.name == "EMAIL_EXISTS") {
                throw UserAlreadyExistsException("Email $email already exists in Firebase")
            }
            Log.error("Error creating user in Firebase", e)
            throw UserCreationException("Error creating user in Firebase: ${e.message}")
        }
    }
    
    fun userExists(uid: String): Boolean {
        return try {
            firebaseAuth.getUser(uid)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun updateUser(uid: String, updatedData: Map<String, Any>): UserRecord {
        val request = UpdateRequest(uid)

        updatedData["email"]?.let { request.setEmail(it as String) }
        updatedData["password"]?.let { request.setPassword(it as String) }
        updatedData["displayName"]?.let { request.setDisplayName(it as String) }
        updatedData["disabled"]?.let { request.setDisabled(it as Boolean) }

        return firebaseAuth.updateUser(request)
    }
    
    fun deleteUser(uid: String) {
        firebaseAuth.deleteUser(uid)
    }
    
    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$*_"
        val random = Random()
        val password = StringBuilder()
        
        for (i in 0 until 16) {
            password.append(chars[random.nextInt(chars.length)])
        }
        
        return password.toString()
    }

    fun getUserByEmail(email: String): UserRecord? {
        return try {
            firebaseAuth.getUserByEmail(email)
        } catch (e: FirebaseAuthException) {
            null
        }
    }

    fun getUserByUid(uid: String): UserRecord? {
        return try {
            firebaseAuth.getUser(uid)
        } catch (e: FirebaseAuthException) {
            null
        }
    }
} 