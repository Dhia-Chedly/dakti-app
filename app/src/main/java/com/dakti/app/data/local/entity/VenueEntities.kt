package com.dakti.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "venues",
    indices = [
        Index(value = ["sportType"]),
        Index(value = ["city"])
    ]
)
data class VenueEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sportType: String,
    val description: String?,
    val address: String,
    val city: String,
    val state: String?,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val pricePerHour: Double,
    val currency: String,
    val amenitiesCsv: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "time_slots",
    foreignKeys = [
        ForeignKey(
            entity = VenueEntity::class,
            parentColumns = ["id"],
            childColumns = ["venueId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["venueId", "startTime", "endTime"], unique = true),
        Index(value = ["venueId", "isAvailable"])
    ]
)
data class TimeSlotEntity(
    @PrimaryKey
    val id: String,
    val venueId: String,
    val startTime: Long,
    val endTime: Long,
    val isAvailable: Boolean,
    val capacity: Int?
)

data class VenueWithTimeSlotsRelation(
    @Embedded
    val venue: VenueEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "venueId"
    )
    val slots: List<TimeSlotEntity>
)
