package skeleton.users.dto

import java.time.OffsetDateTime
import java.util.UUID
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import skeleton.auth.Role

data class UserDTO(
    val id: UUID?,
    val email: String?,
    val name: String?,
    val emailVerified: Boolean,
    val roles: Set<Role>,
    val lastLogin: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class CreateUserDTO(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    val name: String?
)

internal data class InternalUserDTO(
    val id: UUID?,
    val firebaseUid: String,
    val email: String?,
    val name: String?,
    val emailVerified: Boolean,
    val roles: Set<Role>,
    val lastLogin: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class UpdateUserDTO(
    @field:Email(message = "Invalid email format")
    val email: String?,
    
    val name: String?
)