package skeleton.users.resource

import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import skeleton.auth.Authenticated
import skeleton.auth.RequiresRoles
import skeleton.auth.Role
import skeleton.users.dto.CreateUserDTO
import skeleton.users.dto.UpdateUserDTO
import skeleton.users.service.UserService
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.net.URI
import java.util.UUID

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management operations")
@Authenticated
class UserResource {

    @Inject
    lateinit var userService: UserService

    @GET
    @Operation(summary = "List all users")
    @RequiresRoles(Role.ADMIN)
    fun getAll(): Response {
        return Response.ok(userService.findAll()).build()
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Find user by ID")
    fun getById(@PathParam("id") id: UUID): Response {
        return Response.ok(userService.findById(id)).build()
    }

    @GET
    @Path("/firebase/{firebaseUid}")
    @Operation(summary = "Find user by Firebase UID (For internal use only)")
    @RequiresRoles(Role.ADMIN)
    fun getByFirebaseUid(@PathParam("firebaseUid") firebaseUid: String): Response {
        val user = userService.findByFirebaseUid(firebaseUid)
        return if (user != null) {
            Response.ok(user).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @POST
    @Operation(summary = "Create a new user")
    @RequiresRoles(Role.ADMIN)
    fun create(@Valid createUserDTO: CreateUserDTO): Response {
        val newUser = userService.create(createUserDTO)
        return Response
            .created(URI.create("/api/users/${newUser.id}"))
            .entity(newUser)
            .build()
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing user")
    fun update(
        @PathParam("id") id: UUID,
        @Valid updateUserDTO: UpdateUserDTO,
        @HeaderParam("Authorization") authHeader: String
    ): Response {
        return Response.ok(userService.update(id, updateUserDTO)).build()
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a user")
    @RequiresRoles(Role.ADMIN)
    fun delete(@PathParam("id") id: UUID): Response {
        userService.delete(id)
        return Response.noContent().build()
    }

    @PUT
    @Path("/{id}/roles")
    @Operation(summary = "Add a role to user")
    @RequiresRoles(Role.ADMIN)
    fun addRole(
        @PathParam("id") id: UUID,
        role: Role
    ): Response {
        userService.addRole(id, role)
        return Response.ok(userService.findById(id)).build()
    }

    @DELETE
    @Path("/{id}/roles/{role}")
    @Operation(summary = "Remove a role from user")
    @RequiresRoles(Role.ADMIN)
    fun removeRole(
        @PathParam("id") id: UUID,
        @PathParam("role") role: Role
    ): Response {
        userService.removeRole(id, role)
        return Response.ok(userService.findById(id)).build()
    }
} 