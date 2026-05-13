package com.example.nammaraste.presentation.myreports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.domain.ReportStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    navController: NavController,
    viewModel: MyReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("My Reports", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2E7D32),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Reports Yet", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    Text(
                        "Submit your first report using the camera.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report = report)
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: ReportEntity) {
    val statusColor = when (report.status) {
        "SUBMITTED" -> Color(0xFF1565C0)
        "UNDER_REVIEW" -> Color(0xFFE65100)
        "IN_PROGRESS" -> Color(0xFF6A1B9A)
        "RESOLVED" -> Color(0xFF2E7D32)
        else -> Color.Gray
    }

    val statusDisplayName = try {
        ReportStatus.valueOf(report.status).displayName
    } catch (e: Exception) {
        report.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail
            val imageFile = File(report.imageLocalPath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Report thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛣️", fontSize = 36.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    report.ticketId,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    report.issueType.replace("_", " "),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(report.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            statusDisplayName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    if (!report.isSynced) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Pending sync",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}