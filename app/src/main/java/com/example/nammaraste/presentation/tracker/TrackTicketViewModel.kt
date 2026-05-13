package com.example.nammaraste.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackState(
    val isLoading: Boolean = false,
    val report: ReportEntity? = null,
    val errorMessage: String? = null,
    val notFound: Boolean = false
)

@HiltViewModel
class TrackTicketViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _trackState = MutableStateFlow(TrackState())
    val trackState: StateFlow<TrackState> = _trackState

    fun searchTicket(ticketId: String) {
        if (ticketId.isBlank()) {
            _trackState.value = TrackState(errorMessage = "Please enter a ticket ID")
            return
        }
        viewModelScope.launch {
            _trackState.value = TrackState(isLoading = true)
            try {
                val report = reportRepository.trackTicket(ticketId.trim().uppercase())
                if (report != null) {
                    _trackState.value = TrackState(report = report)
                } else {
                    _trackState.value = TrackState(notFound = true)
                }
            } catch (e: Exception) {
                _trackState.value = TrackState(errorMessage = e.message ?: "Search failed")
            }
        }
    }

    fun clearResult() {
        _trackState.value = TrackState()
    }
}