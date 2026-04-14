package com.dakti.app.data.local.dao

interface PlaceholderDao {
    suspend fun getAll(): List<String>
}
