package com.dakti.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.MatchStatus

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = OrganizerEntity::class,
            parentColumns = ["userId"],
            childColumns = ["organizerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VenueEntity::class,
            parentColumns = ["id"],
            childColumns = ["venueId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReservationEntity::class,
            parentColumns = ["id"],
            childColumns = ["reservationId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["organizerId", "scheduledStartTime"]),
        Index(value = ["venueId"]),
        Index(value = ["reservationId"]),
        Index(value = ["status"])
    ]
)
data class MatchEntity(
    @PrimaryKey
    val id: String,
    val organizerId: String,
    val venueId: String,
    val reservationId: String?,
    val title: String,
    val sportType: String,
    val scheduledStartTime: Long,
    val requiredPlayers: Int,
    val status: MatchStatus,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "invitations",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["userId"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OrganizerEntity::class,
            parentColumns = ["userId"],
            childColumns = ["invitedByOrganizerId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchId", "playerId"], unique = true),
        Index(value = ["playerId", "status"]),
        Index(value = ["matchId"]),
        Index(value = ["invitedByOrganizerId"])
    ]
)
data class InvitationEntity(
    @PrimaryKey
    val id: String,
    val matchId: String,
    val playerId: String,
    val invitedByOrganizerId: String?,
    val status: InvitationResponseStatus,
    val message: String?,
    val sentAt: Long,
    val respondedAt: Long?
)

data class MatchWithInvitationsRelation(
    @Embedded
    val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val invitations: List<InvitationEntity>
)

data class MatchWithContextRelation(
    @Embedded
    val match: MatchEntity,
    @Relation(
        parentColumn = "venueId",
        entityColumn = "id"
    )
    val venue: VenueEntity,
    @Relation(
        parentColumn = "reservationId",
        entityColumn = "id"
    )
    val reservation: ReservationEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val invitations: List<InvitationEntity>
)
