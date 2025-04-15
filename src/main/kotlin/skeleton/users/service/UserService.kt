package skeleton.users.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import skeleton.auth.Role
import skeleton.auth.UserSession
import skeleton.firebase.FirebaseAuthService
import skeleton.users.dto.CreateUserDTO
import skeleton.users.dto.InternalUserDTO
import skeleton.users.dto.UpdateUserDTO
import skeleton.users.dto.UserDTO
import skeleton.users.model.User
import skeleton.users.repository.UserRepository
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class UserService {

    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var firebaseAuthService: FirebaseAuthService

    fun findAll(): List<UserDTO> {
        return userRepository.listAllActive().map { mapToDTO(it) }
    }

    fun findById(id: UUID): UserDTO {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        return mapToDTO(user)
    }

    fun findByFirebaseUid(firebaseUid: String): UserDTO? {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: return null
        return mapToDTO(user)
    }

    @Transactional
    fun create(createUserDTO: CreateUserDTO, password: String? = null): UserDTO {
        if (createUserDTO.email.isNotBlank() && userRepository.findByEmail(createUserDTO.email) != null) {
            throw WebApplicationException("A user with this email already exists", Response.Status.CONFLICT)
        }
        val firebaseUser = try {
            firebaseAuthService.createUser(
                createUserDTO.email,
                password,
                createUserDTO.name
            )
        } catch (e: Exception) {
            throw WebApplicationException(
                "Failed to create user in Firebase: ${e.message}", 
                Response.Status.INTERNAL_SERVER_ERROR
            )
        }

        val user = User().apply {
            id = UUID.randomUUID()
            firebaseUid = firebaseUser.uid
            email = createUserDTO.email
            name = createUserDTO.name
            emailVerified = false
            roles = mutableSetOf(Role.USER)
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
            lastLogin = null
        }

        userRepository.persist(user)
        return mapToDTO(user)
    }

    @Transactional
    fun update(id: UUID, updateUserDTO: UpdateUserDTO): UserDTO {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)

        if (updateUserDTO.email != null && 
            updateUserDTO.email != user.email && 
            userRepository.findByEmail(updateUserDTO.email) != null) {
            throw WebApplicationException("This email is already in use", Response.Status.CONFLICT)
        }

        try {
            val updatedData = mutableMapOf<String, Any>()
            
            updateUserDTO.email?.let { updatedData["email"] = it }
            updateUserDTO.name?.let { updatedData["displayName"] = it }
            
            firebaseAuthService.updateUser(user.firebaseUid, updatedData)
        } catch (e: Exception) {
            throw WebApplicationException(
                "Failed to update user in Firebase: ${e.message}", 
                Response.Status.INTERNAL_SERVER_ERROR
            )
        }

        user.apply {
            if (updateUserDTO.email != null) email = updateUserDTO.email
            if (updateUserDTO.name != null) name = updateUserDTO.name
            updatedAt = OffsetDateTime.now()
        }

        userRepository.persist(user)
        return mapToDTO(user)
    }
    
    @Transactional
    fun updateLastLogin(id: UUID) {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        
        user.lastLogin = OffsetDateTime.now()
        userRepository.persist(user)
    }
    
    @Transactional
    fun addRole(id: UUID, role: Role) {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        
        user.roles.add(role)
        userRepository.persist(user)
    }
    
    @Transactional
    fun removeRole(id: UUID, role: Role) {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        
        if (role == Role.USER && user.roles.size == 1) {
            throw WebApplicationException(
                "Cannot remove the USER role if it's the only role", 
                Response.Status.BAD_REQUEST
            )
        }
        
        user.roles.remove(role)
        userRepository.persist(user)
    }
    
    @Transactional
    fun setEmailVerified(id: UUID, verified: Boolean) {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        
        user.emailVerified = verified
        userRepository.persist(user)
    }

    @Transactional
    fun delete(id: UUID) {
        val user = userRepository.findById(id)
            ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
        
        try {
            firebaseAuthService.deleteUser(user.firebaseUid)
        } catch (e: Exception) {
            System.err.println("Failed to delete user from Firebase: ${e.message}")
        }
            
        userRepository.delete(user)
    }

    private fun mapToDTO(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            email = user.email,
            name = user.name,
            emailVerified = user.emailVerified,
            roles = user.roles.toSet(),
            lastLogin = user.lastLogin,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
    
    internal fun mapToInternalDTO(user: User): InternalUserDTO {
        return InternalUserDTO(
            id = user.id,
            firebaseUid = user.firebaseUid,
            email = user.email,
            name = user.name,
            emailVerified = user.emailVerified,
            roles = user.roles.toSet(),
            lastLogin = user.lastLogin,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
    
    fun createUserSession(user: User): UserSession {
        return UserSession(
            userId = user.id!!,
            firebaseUid = user.firebaseUid,
            email = user.email,
            name = user.name,
            roles = user.roles.toSet(),
            lastLogin = user.lastLogin?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
            isEmailVerified = user.emailVerified
        )
    }
} 