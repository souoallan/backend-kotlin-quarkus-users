package skeleton.auth

import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import skeleton.users.service.UserService
import org.jboss.logging.Logger

/**
 * Annotation to mark endpoints that require authentication
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated

@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter : ContainerRequestFilter {
    
    @Inject
    lateinit var tokenVerifier: TokenVerifier
    
    @Inject
    lateinit var userService: UserService
    
    private val logger = Logger.getLogger(AuthenticationFilter::class.java)
    
    override fun filter(requestContext: ContainerRequestContext) {
        val path = requestContext.uriInfo.path
        
        if (path.startsWith("/api/auth/login") || 
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/health") ||
            path.startsWith("/api/docs") ||
            path.startsWith("/api/swagger")) {
            return
        }
        
        val authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(mapOf("error" to "Missing or invalid authorization token"))
                    .build()
            )
            return
        }
        
        val token = authHeader.substring("Bearer ".length).trim()
        
        try {
            val firebaseToken = tokenVerifier.verifyIdToken(token)
            val firebaseUid = firebaseToken.uid
            
            val user = userService.findByFirebaseUid(firebaseUid)
            
            if (user == null) {
                requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                        .entity(mapOf("error" to "User not registered in the system"))
                        .build()
                )
                return
            }
            
            requestContext.setProperty("firebaseUid", firebaseUid)
            
            try {
                userService.updateLastLogin(user.id!!)
            } catch (e: Exception) {
                logger.warn("Failed to update last login: ${e.message}")
            }
            
        } catch (e: Exception) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(mapOf("error" to "Invalid token: ${e.message}"))
                    .build()
            )
        }
    }
} 