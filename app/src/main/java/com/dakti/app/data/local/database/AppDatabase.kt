package com.dakti.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dakti.app.data.local.dao.AssistantDao
import com.dakti.app.data.local.dao.InvitationDao
import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.NotificationDao
import com.dakti.app.data.local.dao.ReservationDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.local.entity.AIRequestEntity
import com.dakti.app.data.local.entity.AISuggestionEntity
import com.dakti.app.data.local.entity.InvitationEntity
import com.dakti.app.data.local.entity.MatchEntity
import com.dakti.app.data.local.entity.NotificationEntity
import com.dakti.app.data.local.entity.OrganizerEntity
import com.dakti.app.data.local.entity.PlayerEntity
import com.dakti.app.data.local.entity.ReservationEntity
import com.dakti.app.data.local.entity.TimeSlotEntity
import com.dakti.app.data.local.entity.UserEntity
import com.dakti.app.data.local.entity.VenueEntity

@Database(
    entities = [
        UserEntity::class,
        OrganizerEntity::class,
        PlayerEntity::class,
        VenueEntity::class,
        TimeSlotEntity::class,
        ReservationEntity::class,
        MatchEntity::class,
        InvitationEntity::class,
        NotificationEntity::class,
        AIRequestEntity::class,
        AISuggestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DbTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun venueDao(): VenueDao
    abstract fun reservationDao(): ReservationDao
    abstract fun matchDao(): MatchDao
    abstract fun invitationDao(): InvitationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun assistantDao(): AssistantDao

    companion object {
        const val DATABASE_NAME: String = "dakti.db"
    }
}
