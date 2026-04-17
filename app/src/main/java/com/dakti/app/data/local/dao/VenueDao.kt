package com.dakti.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dakti.app.data.local.entity.TimeSlotEntity
import com.dakti.app.data.local.entity.VenueEntity
import com.dakti.app.data.local.entity.VenueWithTimeSlotsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface VenueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVenue(venue: VenueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVenues(venues: List<VenueEntity>)

    @Query("SELECT * FROM venues ORDER BY name ASC")
    suspend fun getVenuesOnce(): List<VenueEntity>

    @Transaction
    @Query("SELECT * FROM venues ORDER BY name ASC")
    suspend fun getVenuesWithTimeSlotsOnce(): List<VenueWithTimeSlotsRelation>

    @Transaction
    @Query(
        """
        SELECT * FROM venues
        WHERE (
            :query = '' OR
            LOWER(name) LIKE '%' || LOWER(:query) || '%' OR
            LOWER(city) LIKE '%' || LOWER(:query) || '%' OR
            LOWER(address) LIKE '%' || LOWER(:query) || '%'
        )
        AND (
            :sportType IS NULL OR
            :sportType = '' OR
            LOWER(sportType) = LOWER(:sportType)
        )
        ORDER BY name ASC
        """
    )
    suspend fun searchVenuesWithTimeSlots(
        query: String,
        sportType: String?
    ): List<VenueWithTimeSlotsRelation>

    @Query("SELECT * FROM venues ORDER BY name ASC")
    fun observeVenues(): Flow<List<VenueEntity>>

    @Query("SELECT * FROM venues WHERE id = :venueId LIMIT 1")
    suspend fun getVenueById(venueId: String): VenueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimeSlot(timeSlot: TimeSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimeSlots(timeSlots: List<TimeSlotEntity>)

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlotEntity)

    @Query("SELECT * FROM time_slots WHERE venueId = :venueId ORDER BY startTime ASC")
    fun observeTimeSlotsByVenue(venueId: String): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE venueId = :venueId AND isAvailable = 1 ORDER BY startTime ASC LIMIT 1")
    suspend fun getFirstAvailableTimeSlot(venueId: String): TimeSlotEntity?

    @Transaction
    @Query("SELECT * FROM venues WHERE id = :venueId LIMIT 1")
    fun observeVenueWithTimeSlots(venueId: String): Flow<VenueWithTimeSlotsRelation?>

    @Transaction
    @Query("SELECT * FROM venues WHERE id = :venueId LIMIT 1")
    suspend fun getVenueWithTimeSlotsOnce(venueId: String): VenueWithTimeSlotsRelation?

    @Query("SELECT DISTINCT sportType FROM venues ORDER BY sportType ASC")
    suspend fun getSportTypes(): List<String>

    @Query("DELETE FROM time_slots WHERE venueId = :venueId")
    suspend fun deleteTimeSlotsByVenue(venueId: String)
}
