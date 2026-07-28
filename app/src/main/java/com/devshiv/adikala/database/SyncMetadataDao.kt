// com/devshiv/adikala/database/SyncMetadataDao.kt
package com.devshiv.adikala.database

import androidx.room.*
import com.devshiv.adikala.database.entity.SyncMetadata

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE sync_key = :key LIMIT 1")
    suspend fun getSyncMetadata(key: String): SyncMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: SyncMetadata)

    @Query("DELETE FROM sync_metadata WHERE sync_key = :key")
    suspend fun deleteSyncMetadata(key: String)

    @Query("SELECT * FROM sync_metadata")
    suspend fun getAllSyncMetadata(): List<SyncMetadata>
}