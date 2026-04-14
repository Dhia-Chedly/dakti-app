package com.dakti.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dakti.app.domain.model.NotificationType

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedMatchId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReservationEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedReservationId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "createdAt"]),
        Index(value = ["isRead"]),
        Index(value = ["type"]),
        Index(value = ["relatedMatchId"]),
        Index(value = ["relatedReservationId"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val relatedMatchId: String?,
    val relatedReservationId: String?,
    val createdAt: Long,
    val readAt: Long?
)
