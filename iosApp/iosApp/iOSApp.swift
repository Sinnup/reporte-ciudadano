import SwiftUI
import BackgroundTasks
import UserNotifications

@main
struct iOSApp: App {

    init() {
        // Register the background processing task handler for cloud sync.
        // The identifier must match BGTaskSchedulerPermittedIdentifiers in Info.plist
        // and the constant used in SyncScheduler.ios.kt.
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.espert.reporteciudadano.cloudsync",
            using: nil
        ) { task in
            // The Kotlin SyncScheduler manages scheduling and rescheduling.
            // Mark the OS-level task complete; foreground work is driven by scheduleEagerSync().
            task.setTaskCompleted(success: true)
        }

        // Request authorization to display local notifications (used by SyncFailureNotifier).
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { _, _ in }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}