package skeleton.users.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import java.time.OffsetDateTime
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.ElementCollection
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.FetchType
import jakarta.persistence.CollectionTable
import jakarta.persistence.JoinColumn
import skeleton.auth.Role

@Entity
@Table(name = "users")
class User : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "firebase_uid", nullable = false, unique = true)
    lateinit var firebaseUid: String

    @Column(unique = true)
    var email: String? = null

    var name: String? = null

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "users_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(Role.USER)

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
    
    @Column(name = "last_login")
    var lastLogin: OffsetDateTime? = null
} 