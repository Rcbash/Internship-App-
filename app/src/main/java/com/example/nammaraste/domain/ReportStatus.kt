package com.example.nammaraste.domain

enum class ReportStatus(val displayName: String) {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}