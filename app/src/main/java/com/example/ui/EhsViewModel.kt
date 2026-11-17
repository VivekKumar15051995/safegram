package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccessRole
import com.example.data.CapaAction
import com.example.data.CheckItemState
import com.example.data.EhsRepository
import com.example.data.HazardCategory
import com.example.data.HazardReport
import com.example.data.HazardStatus
import com.example.data.OperatorProfile
import com.example.data.SafetyAudit
import com.example.data.SeverityLevel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class EhsViewModel(
    private val repository: EhsRepository,
    val currentUserId: String,
    val currentUserName: String,
    val currentUserEmail: String = "",
) : ViewModel() {

    val operatorProfile: StateFlow<OperatorProfile?> = repository.observeOperatorProfile(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing operator profile", firestoreError ?: error)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null,
        )

    val hazardsState: StateFlow<UiState<List<HazardReport>>> = repository.observeHazards(currentUserId)
        .map<List<HazardReport>, UiState<List<HazardReport>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing hazards", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load hazard reports"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading,
        )

    val capasState: StateFlow<UiState<List<CapaAction>>> = repository.observeCapas(currentUserId)
        .map<List<CapaAction>, UiState<List<CapaAction>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing CAPAs", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load CAPA actions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading,
        )

    val auditsState: StateFlow<UiState<List<SafetyAudit>>> = repository.observeAudits(currentUserId)
        .map<List<SafetyAudit>, UiState<List<SafetyAudit>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing audits", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load safety audits"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading,
        )

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun submitHazard(
        title: String,
        description: String,
        category: HazardCategory,
        location: String,
        likelihood: Int,
        impact: Int,
        ppeChecklist: List<String>,
        stopWorkIssued: Boolean,
        evidenceUri: String?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.createHazard(
                userId = currentUserId,
                title = title,
                description = description,
                category = category,
                location = location,
                likelihood = likelihood,
                impact = impact,
                ppeChecklist = ppeChecklist,
                stopWorkIssued = stopWorkIssued,
                evidenceUri = evidenceUri,
            )
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = if (stopWorkIssued) {
                    "STOP-WORK order logged and escalated."
                } else {
                    "Hazard report logged to plant registry."
                }
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Unable to submit hazard report."
            }
        }
    }

    fun updateHazardStatus(hazardId: String, newStatus: HazardStatus, stopWorkIssued: Boolean) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.updateHazardStatus(currentUserId, hazardId, newStatus, stopWorkIssued)
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "Hazard status updated to ${newStatus.label}."
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to update hazard status."
            }
        }
    }

    fun deleteHazard(hazardId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.deleteHazard(currentUserId, hazardId)
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "Hazard record removed."
                onDeleted()
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to delete hazard."
            }
        }
    }

    fun createCapa(
        title: String,
        linkedHazardCode: String?,
        ownerName: String,
        dueDateLabel: String,
        priority: SeverityLevel,
        rootCauseSummary: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.createCapa(
                userId = currentUserId,
                title = title,
                linkedHazardCode = linkedHazardCode,
                ownerName = ownerName,
                dueDateLabel = dueDateLabel,
                priority = priority,
                rootCauseSummary = rootCauseSummary,
            )
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "CAPA action created and assigned."
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to create CAPA action."
            }
        }
    }

    fun advanceCapaStep(capa: CapaAction) {
        if (capa.isTerminal) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.advanceCapaStep(
                userId = currentUserId,
                capaId = capa.id,
                currentStep = capa.currentStep.toInt(),
            )
            _isSubmitting.value = false
            result.onSuccess {
                val nextStatus = com.example.data.CapaStatus.fromStep(capa.currentStep.toInt() + 1)
                _feedbackMessage.value = "${capa.code} advanced to ${nextStatus.label}."
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to advance CAPA milestone."
            }
        }
    }

    fun deleteCapa(capaId: String) {
        viewModelScope.launch {
            val result = repository.deleteCapa(currentUserId, capaId)
            result.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to remove CAPA."
            }
        }
    }

    fun createAudit(
        title: String,
        zone: String,
        checklistLabels: List<String>,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.createAudit(
                userId = currentUserId,
                title = title,
                zone = zone,
                inspectorName = currentUserName.ifBlank { "Safety Officer" },
                checklistLabels = checklistLabels,
            )
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "Field inspection clipboard started."
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to create safety audit."
            }
        }
    }

    fun setAuditItemState(audit: SafetyAudit, index: Int, newState: CheckItemState) {
        if (audit.isTerminal) return
        viewModelScope.launch {
            val result = repository.updateAuditCheckItem(
                userId = currentUserId,
                auditId = audit.id,
                currentStates = audit.checklistStates,
                itemIndex = index,
                newState = newState,
            )
            result.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to update inspection item."
            }
        }
    }

    fun completeAudit(audit: SafetyAudit, notes: String) {
        if (audit.isTerminal) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.completeAudit(currentUserId, audit.id, notes)
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "${audit.code} signed off and locked."
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to sign off audit."
            }
        }
    }

    fun seedSampleFacilityData() {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.seedSampleFacilityData(
                userId = currentUserId,
                inspectorName = currentUserName.ifBlank { "Safety Officer" },
            )
            _isSubmitting.value = false
            result.onSuccess {
                _feedbackMessage.value = "Plant-04 sample EHS records loaded."
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to load sample records."
            }
        }
    }

    fun saveOperatorProfile(
        fullName: String,
        badgeId: String,
        role: AccessRole,
        facilityZone: String,
        showToast: Boolean = true,
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.upsertOperatorProfile(
                userId = currentUserId,
                fullName = fullName,
                badgeId = badgeId,
                role = role,
                facilityZone = facilityZone,
                email = currentUserEmail,
            )
            _isSubmitting.value = false
            result.onSuccess {
                if (showToast) {
                    _feedbackMessage.value = "Operator access profile updated (${role.label})."
                }
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "Failed to save operator profile."
            }
        }
    }

    private companion object {
        const val TAG = "EhsViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
