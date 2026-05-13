package com.example.nammaraste.domain

enum class IssueType(val displayName: String) {
    POTHOLE("Pothole"),
    BROKEN_STREETLIGHT("Broken Streetlight"),
    DAMAGED_SIGN("Damaged Sign"),
    WATERLOGGING("Waterlogging"),
    OPEN_MANHOLE("Open Manhole"),
    OTHER("Other")
}