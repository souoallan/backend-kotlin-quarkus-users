package skeleton.auth

import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import skeleton.users.service.UserService
import org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext
import jakarta.ws.rs.container.ResourceInfo
import jakarta.enterprise.inject.spi.CDI

/**
 * Annotation to mark endpoints that require specific roles
 * @param value Array of Roles allowed to access the endpoint
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresRoles(vararg val value: Role)

@Provider
@RequiresRoles
@Priority(Priorities.AUTHORIZATION)
class RoleAuthorizer : ContainerRequestFilter {
    
    @Inject
    lateinit var userService: UserService
    
    @Inject
    lateinit var resourceInfo: ResourceInfo
    
    override fun filter(requestContext: ContainerRequestContext) {
        val firebaseUid = requestContext.getProperty("firebaseUid") as? String
            ?: return
        
        val methodRoles = resourceInfo.resourceMethod?.getAnnotation(RequiresRoles::class.java)?.value
        val classRoles = resourceInfo.resourceClass?.getAnnotation(RequiresRoles::class.java)?.value
        
        if (methodRoles.isNullOrEmpty() && classRoles.isNullOrEmpty()) {
            return
        }
        
        val user = userService.findByFirebaseUid(firebaseUid)
            ?: run {
                requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                        .entity(mapOf("error" to "User not found"))
                        .build()
                )
                return
            }
        
        val requiredRoles = (methodRoles?.toList() ?: emptyList<Role>()) + (classRoles?.toList() ?: emptyList())
        val hasRequiredRole = user.roles.any { it in requiredRoles }
        
        if (!hasRequiredRole) {
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(mapOf("error" to "User does not have the required permissions"))
                    .build()
            )
        }
    }
} 