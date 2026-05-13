package com.example.nammaraste.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE userId = :userId ORDER BY createdAt DESC")
    fun getReportsByUser(userId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE ticketId = :ticketId")
    suspend fun getReportByTicketId(ticketId: String): ReportEntity?

    @Query("UPDATE reports SET isSynced = 1, imageRemoteUrl = :remoteUrl WHERE ticketId = :ticketId")
    suspend fun markAsSynced(ticketId: String, remoteUrl: String)

    @Query("SELECT * FROM reports WHERE isSynced = 0")
    suspend fun getUnsyncedReports(): List<ReportEntity>

    @Delete
    suspend fun deleteReport(report: ReportEntity)
}