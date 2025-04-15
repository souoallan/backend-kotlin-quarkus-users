package skeleton.auth

import java.util.UUID

data class UserSession(
    val userId: UUID,
    val firebaseUid: String,
    val email: String?,
    val name: String?,
    val roles: Set<Role> = setOf(Role.USER),
    val lastLogin: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false
) 