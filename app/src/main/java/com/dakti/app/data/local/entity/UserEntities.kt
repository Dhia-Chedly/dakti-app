package com.dakti.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.dakti.app.domain.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["role"])
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: UserRole,
    val bio: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "organizers",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["isVerified"])
    ]
)
data class OrganizerEntity(
    @PrimaryKey
    val userId: String,
    val rating: Double,
    val totalHostedMatches: Int,
    val organizationName: String?,
    val isVerified: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["preferredSport"])
    ]
)
data class PlayerEntity(
    @PrimaryKey
    val userId: String,
    val preferredSport: String,
    val availabilityNote: String?,
    val skillLevel: String?,
    val rating: Double?,
    val createdAt: Long,
    val updatedAt: Long
)

data class UserWithProfilesRelation(
    @Embedded
    val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val organizer: OrganizerEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val player: PlayerEntity?
)
