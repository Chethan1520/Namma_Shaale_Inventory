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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun registerUser(user: User, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.registerUser(user)
                onResult(true, "Registration Successful")
            } catch (e: Exception) {
                onResult(false, "User already exists or error occurred")
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.passwordHash == password) {
                _currentUser.value = user
                onResult(true, "Login Successful")
            } else {
                onResult(false, "Invalid Email or Password")
            }
        }
    }

    fun resetPassword(email: String, answer: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.securityAnswer.equals(answer, ignoreCase = true)) {
                repository.updateUser(user.copy(passwordHash = newPass))
                onResult(true, "Password Reset Successful")
            } else {
                onResult(false, "Invalid Email or Security Answer")
            }
        }
    }

    private val _userRole = MutableStateFlow<String?>("Principal")
    val userRole = _userRole.asStateFlow()

    fun setUserRole(role: String) {
        _userRole.value = role
    }

    fun logout() {
        _currentUser.value = null
        _userRole.value = null
    }

    private val _selectedInstitutionId = MutableStateFlow<Int?>(null)
    val selectedInstitutionId = _selectedInstitutionId.asStateFlow()

    val allInstitutions: StateFlow<List<Institution>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllInstitutions(user.email)
        else flow { emit(emptyList<Institution>()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedInstitution = _selectedInstitutionId.flatMapLatest { id ->
        if (id != null) flow { emit(repository.getInstitutionById(id)) }
        else flow { emit(null) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAssets: StateFlow<List<Asset>> = combine(_currentUser, _selectedInstitutionId) { user, instId ->
        user to instId
    }.flatMapLatest { (user, instId) ->
        if (user == null) flow { emit(emptyList<Asset>()) }
        else if (instId != null) repository.getAssetsByInstitution(instId)
        else repository.getAllAssets(user.email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAssetsCount: StateFlow<Int> = combine(_currentUser, _selectedInstitutionId) { user, instId ->
        user to instId
    }.flatMapLatest { (user, instId) ->
        if (user == null) flow { emit(0) }
        else repository.getTotalCount(instId, user.email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val repairAssetsCount: StateFlow<Int> = combine(_currentUser, _selectedInstitutionId) { user, instId ->
        user to instId
    }.flatMapLatest { (user, instId) ->
        if (user == null) flow { emit(0) }
        else repository.getRepairCount(instId, user.email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectInstitution(id: Int) {
        _selectedInstitutionId.value = if (id == -1) null else id
    }

    fun addInstitution(name: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val id = repository.insertInstitution(Institution(name = name, userEmail = user.email))
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

    fun addAsset(
        name: String, 
        serialNumber: String, 
        category: String, 
        location: String,
        condition: String, 
        priority: String,
        estimatedRepairCost: Double?,
        note: String? = null, 
        photoUri: String? = null, 
        institutionId: Int? = null
    ) {
        viewModelScope.launch {
            val finalId = institutionId ?: _selectedInstitutionId.value ?: 0
            val newAsset = Asset(
                name = name,
                serialNumber = serialNumber,
                category = category,
                location = location,
                condition = condition,
                priority = priority,
                estimatedRepairCost = estimatedRepairCost,
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
