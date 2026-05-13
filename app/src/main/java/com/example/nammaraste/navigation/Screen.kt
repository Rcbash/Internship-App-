package com.example.nammaraste.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object ReportForm : Screen("report_form/{imagePath}") {
        fun createRoute(imagePath: String): String =
            "report_form/${android.net.Uri.encode(imagePath)}"
    }
    object MyReports : Screen("my_reports")
    object TrackTicket : Screen("track_ticket")
}