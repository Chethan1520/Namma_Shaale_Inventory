package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import kotlinx.coroutines.flow.Flow

class AssetRepository(private val assetDao: AssetDao) {
    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    
    fun getAssetsByInstitution(id: Int): Flow<List<Asset>> = assetDao.getAssetsByInstitution(id)
    fun getTotalCount(id: Int?): Flow<Int> = if (id != null) assetDao.getTotalAssetsCount(id) else assetDao.getGlobalTotalAssetsCount()
    fun getRepairCount(id: Int?): Flow<Int> = if (id != null) assetDao.getAssetsNeedingRepairCount(id) else assetDao.getGlobalAssetsNeedingRepairCount()

    val allInstitutions: Flow<List<Institution>> = assetDao.getAllInstitutions()

    suspend fun insertInstitution(institution: Institution): Long {
        return assetDao.insertInstitution(institution)
    }

    suspend fun getInstitutionById(id: Int): Institution? {
        return assetDao.getInstitutionById(id)
    }

    suspend fun deleteInstitution(institution: Institution) {
        assetDao.deleteInstitution(institution)
    }

    suspend fun insert(asset: Asset): Long {
        return assetDao.insertAsset(asset)
    }

    suspend fun update(asset: Asset) {
        assetDao.updateAsset(asset)
    }

    suspend fun delete(asset: Asset) {
        assetDao.deleteAsset(asset)
    }

    // Health History
    suspend fun recordHealthCheck(healthCheck: HealthCheck) {
        assetDao.insertHealthCheck(healthCheck)
    }

    fun getHistoryForAsset(assetId: Int): Flow<List<HealthCheck>> {
        return assetDao.getHealthHistoryForAsset(assetId)
    }
}
