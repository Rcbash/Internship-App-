package com.example.nammaraste.presentation.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.nammaraste.domain.IssueType
import com.example.nammaraste.domain.Severity
import com.example.nammaraste.navigation.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    navController: NavController,
    imagePath: String,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var selectedIssueType by remember { mutableStateOf(IssueType.POTHOLE) }
    var selectedSeverity by remember { mutableStateOf(Severity.MEDIUM) }
    var description by remember { mutableStateOf("") }
    var issueTypeExpanded by remember { mutableStateOf(false) }
    var severityExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            viewModel.fetchLocation()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.fetchLocation()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) showSuccessDialog = true
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("🎉 Report Submitted!") },
            text = {
                Column {
                    Text("Your report has been submitted successfully.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ticket ID:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        uiState.ticketId,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Save this ID to track your report status.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Camera.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Report Issue", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1565C0),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Image preview
            if (imagePath.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(imagePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Captured image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // GPS Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.latitude != 0.0) Color(0xFFE8F5E9)
                    else Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isFetchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Fetching GPS location...")
                    } else if (uiState.latitude != 0.0) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Location Captured", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                            Text(
                                "Lat: ${"%.4f".format(uiState.latitude)}, " +
                                        "Lng: ${"%.4f".format(uiState.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Location Not Available", color = Color(0xFFE65100))
                            TextButton(
                                onClick = { viewModel.fetchLocation() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Retry", color = Color(0xFFE65100))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Issue Type Dropdown
            Text("Issue Type", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = issueTypeExpanded,
                onExpandedChange = { issueTypeExpanded = !issueTypeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedIssueType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issueTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = issueTypeExpanded,
                    onDismissRequest = { issueTypeExpanded = false }
                ) {
                    IssueType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                selectedIssueType = type
                                issueTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Severity Dropdown
            Text("Severity", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = severityExpanded,
                onExpandedChange = { severityExpanded = !severityExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSeverity.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when (selectedSeverity) {
                            Severity.HIGH -> Color(0xFFD32F2F)
                            Severity.MEDIUM -> Color(0xFFE65100)
                            Severity.LOW -> Color(0xFF2E7D32)
                        }
                    )
                )
                ExposedDropdownMenu(
                    expanded = severityExpanded,
                    onDismissRequest = { severityExpanded = false }
                ) {
                    Severity.entries.forEach { sev ->
                        val color = when (sev) {
                            Severity.HIGH -> Color(0xFFD32F2F)
                            Severity.MEDIUM -> Color(0xFFE65100)
                            Severity.LOW -> Color(0xFF2E7D32)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(sev.displayName, color = color, fontWeight = FontWeight.SemiBold)
                            },
                            onClick = {
                                selectedSeverity = sev
                                severityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text("Description (Optional)", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe the issue...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.submitReport(
                        imagePath = imagePath,
                        issueType = selectedIssueType,
                        severity = selectedSeverity,
                        description = description
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}