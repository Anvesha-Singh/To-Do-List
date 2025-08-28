# To-Do List App (Android, Kotlin, Jetpack Compose)

This project is an Android To-Do List application built with **Kotlin** using **Jetpack Compose** and the **MVVM architecture pattern**.  
It uses **Room** for local data storage, **WorkManager** for scheduling notifications, and **Navigation Compose** for bottom navigation.  
The app is modular and follows clean architecture principles: UI (screens), ViewModel (state & logic), Repository (data access), and Database (Room entities/DAO).

---

## 📂 Project Structure

### 1. Data Layer
- **`data/database/AppDatabase.kt`**  
  Defines the Room `AppDatabase` as the main entry point to the local database.  
  Registers `TaskDao` for database operations.

- **`data/database/Task.kt`**  
  Entity class representing a task in the database.  
  Contains fields such as `id`, `title`, `description`, `deadline`, `priority`, `isCompleted`.

- **`data/database/TaskDao.kt`**  
  Data Access Object (DAO) defining SQL queries for CRUD operations on tasks.  
  Includes methods like `insert`, `update`, `delete`, and `getAllTasks`.

- **`data/repository/TaskRepository.kt`**  
  Repository layer providing an abstraction over `TaskDao`.  
  Handles all database interactions and exposes methods for the ViewModel.

---

### 2. ViewModel Layer
- **`viewmodel/TaskViewModel.kt`**  
  Main ViewModel for managing tasks.  
  Uses `TaskRepository` to fetch/update task data.  
  Exposes `LiveData`/`StateFlow` for UI.

- **`viewmodel/TaskViewModelFactory.kt`**  
  Factory class to create `TaskViewModel` instances with dependencies injected.

- **`viewmodel/ViewModelProvider.kt`**  
  Custom utility to provide ViewModel instances with the correct repository.

---

### 3. UI Layer
- **`screens/TasksScreen.kt`**  
  UI for viewing and managing all tasks.

- **`screens/DailyWeeklyScreen.kt`**  
  Placeholder screen for daily/weekly task breakdown.

- **`screens/CalendarScreen.kt`**  
  Placeholder screen for calendar view of tasks.

- **`screens/StatsScreen.kt`**  
  Placeholder screen for productivity stats (later to integrate with MPAndroidChart).

- **`ui/theme/Color.kt`**  
  Defines app colors for Compose theme.

- **`ui/theme/Theme.kt`**  
  Configures Material theme for Compose.

- **`ui/theme/Type.kt`**  
  Defines typography styles for Compose UI.

---

### 4. Navigation
- **`navigation/NavGraph.kt`**  
  Sets up bottom navigation with 5 tabs:  
  - Tasks  
  - Daily/Weekly  
  - Calendar  
  - Stats  
  - Settings  

---

### 5. Notifications
- **`notification/NotificationHelper.kt`**  
  Utility class to show notifications.

- **`notification/TaskReminderWorker.kt`**  
  WorkManager worker that triggers task reminder notifications at scheduled times.

---

### 6. Entry Point
- **`MainActivity.kt`**  
  App’s main entry point.  
  Sets up Compose `Scaffold`, navigation, and connects to the `NavGraph`.

---

### 7. Resources
- **`res/values/strings.xml`**  
  Contains string resources (tab names, labels, etc.).

- **`AndroidManifest.xml`**  
  Declares permissions, services (WorkManager), and main activity.

---

### 8. Build & Config
- **`app/build.gradle.kts`**  
  Module-level Gradle file with dependencies for:
  - Jetpack Compose
  - Room
  - WorkManager
  - Navigation Compose
  - MPAndroidChart

- **`build.gradle.kts`**, **`settings.gradle.kts`**, **`gradle.properties`**, **`gradle-wrapper.properties`**  
  Standard Gradle configuration files.

---

## 🚀 Features Implemented
- MVVM architecture set up  
- Room database with `Task` entity  
- Repository pattern  
- ViewModel integration  
- Bottom navigation with 5 tabs  
- Basic screens scaffolded  
- WorkManager + Notifications setup (reminder system)  

---

## 🛠️ Planned Features
- Daily/weekly task view  
- Calendar integration  
- Productivity statistics with MPAndroidChart  
- Task notifications with deadlines  
- Settings screen  

