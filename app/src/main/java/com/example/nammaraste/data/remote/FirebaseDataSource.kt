package com.example.nammaraste.data.remote

import android.net.Uri
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /**
     * Uploads a local image file to Firebase Storage and returns the download URL.
     */
    suspend fun uploadImage(localPath: String, ticketId: String): String {
        val file = File(localPath)
        if (!file.exists()) return ""

        val ref = storage.reference
            .child(Constants.STORAGE_REPORTS_FOLDER)
            .child("$ticketId.jpg")

        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Saves a report document to Firestore.
     */
    suspend fun saveReport(report: ReportEntity, imageUrl: String) {
        val data = hashMapOf(
            "ticketId" to report.ticketId,
            "userId" to report.userId,
            "issueType" to report.issueType,
            "severity" to report.severity,
            "latitude" to report.latitude,
            "longitude" to report.longitude,
            "imageUrl" to imageUrl,
            "status" to report.status,
            "description" to report.description,
            "createdAt" to report.createdAt
        )
        firestore.collection(Constants.REPORTS_COLLECTION)
            .document(report.ticketId)
            .set(data)
            .await()
    }

    /**
     * Fetches a report from Firestore by ticket ID.
     */
    suspend fun getReportByTicketId(ticketId: String): Map<String, Any>? {
        val snapshot = firestore.collection(Constants.REPORTS_COLLECTION)
            .document(ticketId)
            .get()
            .await()
        return if (snapshot.exists()) snapshot.data else null
    }
}