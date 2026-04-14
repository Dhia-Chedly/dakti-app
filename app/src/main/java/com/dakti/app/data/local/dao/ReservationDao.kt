package com.dakti.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dakti.app.data.local.entity.ReservationEntity
import com.dakti.app.data.local.entity.ReservationWithDetailsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReservation(reservation: ReservationEntity)

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE id = :reservationId")
    suspend fun deleteReservation(reservationId: String)

    @Query("SELECT * FROM reservations WHERE id = :reservationId LIMIT 1")
    suspend fun getReservationById(reservationId: String): ReservationEntity?

    @Transaction
    @Query("SELECT * FROM reservations WHERE id = :reservationId LIMIT 1")
    suspend fun getReservationWithDetails(reservationId: String): ReservationWithDetailsRelation?

    @Transaction
    @Query("SELECT * FROM reservations WHERE organizerId = :organizerId ORDER BY createdAt DESC")
    suspend fun getReservationsWithDetailsByOrganizer(
        organizerId: String
    ): List<ReservationWithDetailsRelation>

    @Transaction
    @Query("SELECT * FROM reservations WHERE organizerId = :organizerId ORDER BY createdAt DESC")
    fun observeReservationsWithDetailsByOrganizer(
        organizerId: String
    ): Flow<List<ReservationWithDetailsRelation>>
}
