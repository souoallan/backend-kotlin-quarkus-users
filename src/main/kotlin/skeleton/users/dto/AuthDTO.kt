package skeleton.users.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterUserDTO(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String,
    
    val name: String? = null
)

data class LoginDTO(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
) 