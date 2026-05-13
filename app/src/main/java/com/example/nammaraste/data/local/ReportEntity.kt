package com.example.nammaraste.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a single infrastructure defect report.
 * isSynced = false means it's queued for Firebase upload via WorkManager.
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: String,
    val userId: String,
    val issueType: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val imageLocalPath: String,
    val imageRemoteUrl: String = "",
    val status: String = "SUBMITTED",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)