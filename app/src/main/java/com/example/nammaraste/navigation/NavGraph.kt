package com.example.nammaraste.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nammaraste.presentation.auth.LoginScreen
import com.example.nammaraste.presentation.auth.SignupScreen
import com.example.nammaraste.presentation.home.HomeScreen
import com.example.nammaraste.presentation.myreports.MyReportsScreen
import com.example.nammaraste.presentation.report.CameraScreen
import com.example.nammaraste.presentation.report.ReportFormScreen
import com.example.nammaraste.presentation.splash.SplashScreen
import com.example.nammaraste.presentation.tracker.TrackTicketScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }

        composable(
            route = Screen.ReportForm.route,
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
            ReportFormScreen(
                navController = navController,
                imagePath = android.net.Uri.decode(imagePath)
            )
        }

        composable(Screen.MyReports.route) {
            MyReportsScreen(navController = navController)
        }

        composable(Screen.TrackTicket.route) {
            TrackTicketScreen(navController = navController)
        }
    }
}