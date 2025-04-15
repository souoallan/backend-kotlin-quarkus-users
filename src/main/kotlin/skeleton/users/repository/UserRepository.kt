package skeleton.users.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import skeleton.users.model.User
import java.util.UUID

@ApplicationScoped
class UserRepository : PanacheRepositoryBase<User, UUID> {
    
    fun findByFirebaseUid(firebaseUid: String): User? {
        return find("firebaseUid", firebaseUid).firstResult()
    }
    
    fun findByEmail(email: String): User? {
        return find("email", email).firstResult()
    }
    
    fun listAllActive(): List<User> {
        return listAll()
    }
    
    override fun deleteById(id: UUID): Boolean {
        return super.deleteById(id)
    }
} 