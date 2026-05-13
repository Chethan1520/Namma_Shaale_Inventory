package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    // Institution operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInstitution(institution: Institution): Long

    @Query("SELECT * FROM institutions ORDER BY name ASC")
    fun getAllInstitutions(): Flow<List<Institution>>

    @Query("SELECT * FROM institutions WHERE id = :id")
    suspend fun getInstitutionById(id: Int): Institution?

    @Delete
    suspend fun deleteInstitution(institution: Institution)

    // Asset operations
    @Query("SELECT * FROM assets WHERE institutionId = :institutionId ORDER BY lastChecked DESC")
    fun getAssetsByInstitution(institutionId: Int): Flow<List<Asset>>

    @Query("SELECT * FROM assets ORDER BY lastChecked DESC")
    fun getAllAssets(): Flow<List<Asset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset): Long

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("SELECT COUNT(*) FROM assets WHERE institutionId = :institutionId")
    fun getTotalAssetsCount(institutionId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM assets")
    fun getGlobalTotalAssetsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assets WHERE condition = 'Needs Repair' AND institutionId = :institutionId")
    fun getAssetsNeedingRepairCount(institutionId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM assets WHERE condition = 'Needs Repair'")
    fun getGlobalAssetsNeedingRepairCount(): Flow<Int>

    // Health Check History operations
    @Insert
    suspend fun insertHealthCheck(healthCheck: HealthCheck)

    @Query("SELECT * FROM health_checks WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun getHealthHistoryForAsset(assetId: Int): Flow<List<HealthCheck>>
}
