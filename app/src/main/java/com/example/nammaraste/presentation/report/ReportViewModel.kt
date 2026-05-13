package com.example.nammaraste.presentation.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.data.repository.ReportRepository
import com.example.nammaraste.domain.IssueType
import com.example.nammaraste.domain.Severity
import com.example.nammaraste.utils.Constants
import com.example.nammaraste.utils.LocationHelper
import com.example.nammaraste.utils.TicketIdGenerator
import com.example.nammaraste.worker.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val ticketId: String = "",
    val errorMessage: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isFetchingLocation: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val locationHelper: LocationHelper,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingLocation = true, errorMessage = null)
            try {
                val location = locationHelper.getCurrentLocation()
                if (location.latitude != 0.0) {
                    _uiState.value = _uiState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isFetchingLocation = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isFetchingLocation = false,
                        errorMessage = "Location unavailable. Ensure GPS is ON and you are outdoors."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isFetchingLocation = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun submitReport(
        imagePath: String,
        issueType: IssueType,
        severity: Severity,
        description: String
    ) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val ticketId = TicketIdGenerator.generate()

        val report = ReportEntity(
            ticketId = ticketId,
            userId = userId,
            issueType = issueType.name,
            severity = severity.name,
            latitude = _uiState.value.latitude,
            longitude = _uiState.value.longitude,
            imageLocalPath = imagePath,
            description = description
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reportRepository.submitReport(report)
            result.fold(
                onSuccess = {
                    scheduleSyncWork()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        ticketId = ticketId
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Submission failed"
                    )
                }
            )
        }
    }

    private fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(Constants.SYNC_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            Constants.SYNC_WORK_TAG,
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}