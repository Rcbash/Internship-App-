package com.example.nammaraste.presentation.myreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.data.repository.ReportRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _reports = MutableStateFlow<List<ReportEntity>>(emptyList())
    val reports: StateFlow<List<ReportEntity>> = _reports

    init {
        loadReports()
    }

    private fun loadReports() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            reportRepository.getMyReports(userId).collect { reportList ->
                _reports.value = reportList
            }
        }
    }
}