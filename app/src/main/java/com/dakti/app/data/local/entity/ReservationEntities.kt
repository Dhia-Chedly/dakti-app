package com.dakti.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.dakti.app.domain.model.ReservationStatus

@Entity(
    tableName = "reservations",
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
            entity = TimeSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeSlotId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["organizerId", "createdAt"]),
        Index(value = ["venueId"]),
        Index(value = ["timeSlotId"], unique = true),
        Index(value = ["status"])
    ]
)
data class ReservationEntity(
    @PrimaryKey
    val id: String,
    val organizerId: String,
    val venueId: String,
    val timeSlotId: String,
    val status: ReservationStatus,
    val totalPrice: Double?,
    val currency: String?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class ReservationWithDetailsRelation(
    @Embedded
    val reservation: ReservationEntity,
    @Relation(
        parentColumn = "venueId",
        entityColumn = "id"
    )
    val venue: VenueEntity,
    @Relation(
        parentColumn = "timeSlotId",
        entityColumn = "id"
    )
    val timeSlot: TimeSlotEntity
)
