package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AssetViewModel(private val repository: AssetRepository) : ViewModel() {

    private val _selectedInstitutionId = MutableStateFlow<Int?>(null)
    val selectedInstitutionId = _selectedInstitutionId.asStateFlow()

    val allInstitutions: StateFlow<List<Institution>> = repository.allInstitutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedInstitution = _selectedInstitutionId.flatMapLatest { id ->
        if (id != null) flow { emit(repository.getInstitutionById(id)) }
        else flow { emit(null) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAssets: StateFlow<List<Asset>> = _selectedInstitutionId.flatMapLatest { id ->
        if (id != null) repository.getAssetsByInstitution(id)
        else repository.allAssets
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAssetsCount: StateFlow<Int> = _selectedInstitutionId.flatMapLatest { id ->
        repository.getTotalCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val repairAssetsCount: StateFlow<Int> = _selectedInstitutionId.flatMapLatest { id ->
        repository.getRepairCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectInstitution(id: Int) {
        _selectedInstitutionId.value = if (id == -1) null else id
    }

    fun addInstitution(name: String) {
        viewModelScope.launch {
            val id = repository.insertInstitution(Institution(name = name))
            _selectedInstitutionId.value = id.toInt()
        }
    }

    fun deleteInstitution(institution: Institution) {
        viewModelScope.launch {
            if (_selectedInstitutionId.value == institution.id) {
                _selectedInstitutionId.value = null
            }
            repository.deleteInstitution(institution)
        }
    }

    fun addAsset(name: String, serialNumber: String, category: String, condition: String, note: String? = null, photoUri: String? = null, institutionId: Int? = null) {
        viewModelScope.launch {
            val finalId = institutionId ?: _selectedInstitutionId.value ?: 0
            val newAsset = Asset(
                name = name,
                serialNumber = serialNumber,
                category = category,
                condition = condition,
                note = note,
                photoUri = photoUri,
                institutionId = finalId
            )
            val assetId = repository.insert(newAsset)
            
            // Record initial health state
            repository.recordHealthCheck(
                HealthCheck(
                    assetId = assetId.toInt(),
                    condition = condition,
                    note = note ?: "Initial Registration"
                )
            )
        }
    }

    fun updateAssetCondition(asset: Asset, newCondition: String, newNote: String? = asset.note) {
        viewModelScope.launch {
            val updatedAsset = asset.copy(
                condition = newCondition,
                note = newNote,
                lastChecked = System.currentTimeMillis()
            )
            repository.update(updatedAsset)
            
            // Log history
            repository.recordHealthCheck(
                HealthCheck(
                    assetId = asset.id,
                    condition = newCondition,
                    note = newNote
                )
            )
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            repository.delete(asset)
        }
    }

    fun getHistoryForAsset(assetId: Int): Flow<List<HealthCheck>> {
        return repository.getHistoryForAsset(assetId)
    }
}

class AssetViewModelFactory(private val repository: AssetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
