package com.example.myproxy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM nodes")
    fun observeAllNodes(): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes")
    suspend fun getAllNodes(): List<NodeEntity>

    @Query("SELECT * FROM nodes WHERE selected = 1 LIMIT 1")
    suspend fun getSelectedNode(): NodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<NodeEntity>)

    @Query("DELETE FROM nodes")
    suspend fun clearAll()

    @Query("UPDATE nodes SET selected = 0")
    suspend fun clearSelected()

    @Query("UPDATE nodes SET selected = 1 WHERE id = :id")
    suspend fun setSelectedById(id: Int)

    @Query("UPDATE nodes SET latency = :latency WHERE id = :id")
    suspend fun updateLatency(id: Int, latency: Int)

    @Transaction
    suspend fun replaceAll(nodes: List<NodeEntity>) {
        clearAll()
        insertAll(nodes)
    }
}
