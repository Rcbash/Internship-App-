package com.example.nammaraste.presentation.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nammaraste.data.local.ReportEntity
import com.example.nammaraste.domain.ReportStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackTicketScreen(
    navController: NavController,
    viewModel: TrackTicketViewModel = hiltViewModel()
) {
    val trackState by viewModel.trackState.collectAsState()
    var ticketInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Track Ticket", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE65100),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("🔍", fontSize = 64.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your Ticket ID",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "Example: NR-2026-12345",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = ticketInput,
                onValueChange = { ticketInput = it.uppercase() },
                label = { Text("Ticket ID") },
                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                trailingIcon = {
                    if (ticketInput.isNotBlank()) {
                        IconButton(onClick = { ticketInput = ""; viewModel.clearResult() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    viewModel.searchTicket(ticketInput)
                }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.searchTicket(ticketInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !trackState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                if (trackState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result
            when {
                trackState.report != null -> TicketResultCard(report = trackState.report!!)
                trackState.notFound -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("❓", fontSize = 40.sp)
                            Text("Ticket Not Found", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text(
                                "No report found with ID: ${ticketInput}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                trackState.errorMessage != null -> {
                    Text(trackState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun TicketResultCard(report: ReportEntity) {
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

    // Timeline steps
    val steps = listOf("Submitted", "Under Review", "In Progress", "Resolved")
    val currentStep = when (report.status) {
        "SUBMITTED" -> 0
        "UNDER_REVIEW" -> 1
        "IN_PROGRESS" -> 2
        "RESOLVED" -> 3
        else -> 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(report.ticketId, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), fontSize = 18.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow("Issue Type", report.issueType.replace("_", " "))
            InfoRow("Severity", report.severity)
            InfoRow(
                "Submitted On",
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(report.createdAt))
            )
            if (report.latitude != 0.0) {
                InfoRow("Location", "${"%.4f".format(report.latitude)}, ${"%.4f".format(report.longitude)}")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Status", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Status badge
            Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.1f)) {
                Text(
                    statusDisplayName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline
            Text("Progress Timeline", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (index <= currentStep) statusColor else Color(0xFFE0E0E0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (index < currentStep) "✓" else "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        step,
                        color = if (index <= currentStep) Color.Black else Color.LightGray,
                        fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 11.dp)
                            .width(2.dp)
                            .height(20.dp)
                    ) {
                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight().width(2.dp),
                            color = if (index < currentStep) statusColor else Color(0xFFE0E0E0),
                            thickness = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            "$label:",
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.width(110.dp),
            fontSize = 14.sp
        )
        Text(value, fontSize = 14.sp)
    }
}