package skeleton.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

@ApplicationScoped
class TokenVerifier {
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    fun verifyIdToken(idToken: String): FirebaseToken {
        try {
            return firebaseAuth.verifyIdToken(idToken)
        } catch (e: Exception) {
            throw WebApplicationException("Invalid or expired token", Response.Status.UNAUTHORIZED)
        }
    }
    
    fun getUserIdFromToken(idToken: String): String {
        return verifyIdToken(idToken).uid
    }
    
    fun isTokenValid(idToken: String): Boolean {
        return try {
            verifyIdToken(idToken)
            true
        } catch (e: Exception) {
            false
        }
    }
} 