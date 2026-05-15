# Namma-Raste Reporter

Namma-Raste Reporter is a modern Android application designed for citizens to report infrastructure defects (like potholes, broken streetlights, etc.) directly to the authorities. The app captures the location and photos of the issues, ensuring accurate reporting and tracking.

## 🚀 Features

- **Issue Reporting:** Quickly report infrastructure problems with details like issue type, severity, and description.
- **Photo Capture:** Integrated camera functionality to attach visual proof to reports.
- **Location Tracking:** Automatically captures GPS coordinates for precise defect localization.
- **Offline Support:** Uses a local Room database to save reports even when offline.
- **Background Syncing:** Leverages WorkManager to automatically sync reports to Firebase once an internet connection is available.
- **Ticket Tracking:** Users can track the status of their submitted reports using unique Ticket IDs.
- **My Reports:** A dedicated section for users to view their history of reported issues.

## 🛠 Tech Stack

- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, reactive user interface.
- **Architecture:** MVVM (Model-View-ViewModel) following Clean Architecture principles.
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for robust and scalable DI.
- **Database:** [Room](https://developer.android.com/training/data-storage/room) for local persistent storage.
- **Network & Backend:** [Firebase](https://firebase.google.com/) (Auth, Storage, and Firestore) for user authentication and remote data management.
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for efficient image loading in Compose.
- **Background Tasks:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable background synchronization.
- **Camera:** [CameraX](https://developer.android.com/training/camerax) for handling camera functionality.

## 📦 Project Structure

- `data/`: Contains Local (Room) and Remote (Firebase) data sources, entities, and repository implementations.
- `presentation/`: UI components, Composables, and ViewModels organized by feature (Report, Tracker, MyReports).
- `utils/`: Helper classes for location, ID generation, and constants.

## 🏁 Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/NammaRasteReporter.git
    ```
2.  **Firebase Setup:**
    - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android App with the package name `com.example.nammaraste`.
    - Download the `google-services.json` file and place it in the `app/` directory.
    - Enable Authentication, Firestore, and Storage in the Firebase Console.
3.  **Build & Run:**
    - Open the project in Android Studio.
    - Sync Gradle and run the app on an emulator or a physical device.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
