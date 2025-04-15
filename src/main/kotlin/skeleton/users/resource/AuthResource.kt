package skeleton.users.resource

import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import skeleton.auth.Authenticated
import skeleton.auth.TokenVerifier
import skeleton.firebase.FirebaseAuthService
import skeleton.users.dto.CreateUserDTO
import skeleton.users.dto.LoginDTO
import skeleton.users.dto.RegisterUserDTO
import skeleton.users.service.UserService
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.net.URI

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Authentication related operations")
class AuthResource {

    @Inject
    lateinit var userService: UserService
    
    @Inject
    lateinit var firebaseAuthService: FirebaseAuthService
    
    @Inject
    lateinit var tokenVerifier: TokenVerifier

    @GET
    @Path("/check/{uid}")
    @Operation(summary = "Check if a user with the provided UID exists")
    fun checkUserExists(@PathParam("firebaseUid") firebaseUid: String): Response {
        val user = userService.findByFirebaseUid(firebaseUid)
        return if (user != null) {
            Response.ok(mapOf("exists" to true, "user" to user)).build()
        } else {
            Response.ok(mapOf("exists" to false)).build()
        }
    }
    
    @POST
    @Path("/register")
    @Operation(summary = "Register in the application")
    fun createNewUser(@Valid registerUserDTO: RegisterUserDTO): Response {
        try {
            val createUserDTO = CreateUserDTO(
                email = registerUserDTO.email,
                name = registerUserDTO.name
            )
            
            val newUser = userService.create(createUserDTO, registerUserDTO.password)
            
            return Response
                .created(URI.create("/api/users/${newUser.id}"))
                .entity(mapOf(
                    "message" to "User created successfully",
                    "user" to newUser
                ))
                .build()
                
        } catch (e: Exception) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Failed to create user: ${e.message}"))
                .build()
        }
    }
    
    @POST
    @Path("/verify-token")
    @Operation(summary = "Verify token and get user information")
    fun verifyToken(@HeaderParam("Authorization") authHeader: String): Response {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Authorization header must be 'Bearer <token>'"))
                .build()
        }
        
        val token = authHeader.substring("Bearer ".length).trim()
        
        try {
            val firebaseToken = tokenVerifier.verifyIdToken(token)
            val firebaseUid = firebaseToken.uid
            
            val user = userService.findByFirebaseUid(firebaseUid)
            
            if (user == null) {
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf(
                        "verified" to true,
                        "exists" to false,
                        "message" to "Token is valid but user is not registered in the system"
                    ))
                    .build()
            }
            
            userService.updateLastLogin(user.id!!)
            
            return Response
                .ok()
                .entity(mapOf(
                    "verified" to true,
                    "exists" to true,
                    "user" to user
                ))
                .build()
                
        } catch (e: Exception) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(mapOf(
                    "verified" to false,
                    "error" to "Invalid token: ${e.message}"
                ))
                .build()
        }
    }
    
    @GET
    @Path("/me")
    @Authenticated
    @Operation(summary = "Get current authenticated user information")
    fun getCurrentUser(@HeaderParam("Authorization") authHeader: String): Response {
        val token = authHeader.substring("Bearer ".length).trim()
        val firebaseUid = tokenVerifier.getUserIdFromToken(token)
        
        val user = userService.findByFirebaseUid(firebaseUid)
            ?: return Response
                .status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()
        
        return Response.ok(user).build()
    }
} 