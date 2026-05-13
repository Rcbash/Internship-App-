package com.example.nammaraste.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nammaraste.data.repository.ReportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reportRepository: ReportRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedReports = reportRepository.getUnsyncedReports()
            var allSuccessful = true
            
            for (report in unsyncedReports) {
                val success = reportRepository.syncReport(report)
                if (!success) allSuccessful = false
            }
            
            if (allSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}