package com.example.nammaraste.data.repository

import com.example.nammaraste.data.local.ReportDao
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.data.remote.FirebaseDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao,
    private val firebaseDataSource: FirebaseDataSource
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    /**
     * Saves report to Room first (offline-safe), then attempts Firebase sync.
     * Returns the saved report entity immediately after local save.
     */
    suspend fun submitReport(report: ReportEntity): Result<ReportEntity> {
        return try {
            // Step 1: Save locally always - This is the primary source of truth
            reportDao.insertReport(report)

            // Step 2: Attempt Firebase sync in a separate scope so we don't block the caller
            // This ensures the "Submit" button doesn't hang even if network is dead or rules are wrong
            repositoryScope.launch {
                try {
                    val imageUrl = firebaseDataSource.uploadImage(report.imageLocalPath, report.ticketId)
                    firebaseDataSource.saveReport(report, imageUrl)
                    reportDao.markAsSynced(report.ticketId, imageUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Returns a Flow of all reports for the given user, sorted by newest first. */
    fun getMyReports(userId: String): Flow<List<ReportEntity>> =
        reportDao.getReportsByUser(userId)

    /** Looks up a ticket by ID — checks Room first, then Firestore. */
    suspend fun trackTicket(ticketId: String): ReportEntity? {
        val local = reportDao.getReportByTicketId(ticketId)
        if (local != null) return local

        // Fallback to Firestore if not in local DB
        return try {
            val remote = firebaseDataSource.getReportByTicketId(ticketId)
            remote?.let {
                ReportEntity(
                    ticketId = it["ticketId"] as? String ?: ticketId,
                    userId = it["userId"] as? String ?: "",
                    issueType = it["issueType"] as? String ?: "",
                    severity = it["severity"] as? String ?: "",
                    latitude = (it["latitude"] as? Double) ?: 0.0,
                    longitude = (it["longitude"] as? Double) ?: 0.0,
                    imageLocalPath = "",
                    imageRemoteUrl = it["imageUrl"] as? String ?: "",
                    status = it["status"] as? String ?: "SUBMITTED",
                    description = it["description"] as? String ?: "",
                    createdAt = (it["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    isSynced = true
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Returns all reports that haven't been synced to Firebase. */
    suspend fun getUnsyncedReports(): List<ReportEntity> =
        reportDao.getUnsyncedReports()

    /** Marks a report as synced after WorkManager uploads it. */
    suspend fun markAsSynced(ticketId: String, remoteUrl: String) =
        reportDao.markAsSynced(ticketId, remoteUrl)

    /**
     * Dedicated sync function for WorkManager to use.
     */
    suspend fun syncReport(report: ReportEntity): Boolean {
        return try {
            val imageUrl = if (report.imageRemoteUrl.isBlank()) {
                firebaseDataSource.uploadImage(report.imageLocalPath, report.ticketId)
            } else {
                report.imageRemoteUrl
            }
            firebaseDataSource.saveReport(report, imageUrl)
            reportDao.markAsSynced(report.ticketId, imageUrl)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}