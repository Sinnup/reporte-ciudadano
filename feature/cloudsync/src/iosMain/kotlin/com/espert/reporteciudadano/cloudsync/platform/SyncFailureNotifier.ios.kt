@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.espert.reporteciudadano.cloudsync.platform

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.*
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.sync_notification_action_retry
import reporteciudadano.shared.generated.resources.sync_notification_body
import reporteciudadano.shared.generated.resources.sync_notification_title

private const val RETRY_SYNC_ACTION_ID = "RETRY_SYNC"
private const val SYNC_FAILURE_CATEGORY_ID = "SYNC_FAILURE"

actual object SyncFailureNotifier {

    actual fun notifySyncFailure(reportId: String, reportTitle: String) {
        val title = runBlocking { getString(Res.string.sync_notification_title) }
        val body = runBlocking { getString(Res.string.sync_notification_body, reportTitle) }
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setCategoryIdentifier(SYNC_FAILURE_CATEGORY_ID)
            setUserInfo(mapOf<Any?, Any?>("reportId" to reportId))
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "sync_failure_$reportId",
            content = content,
            trigger = null // Deliver immediately
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { _ -> }
    }

    actual fun cancelNotification(reportId: String) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf("sync_failure_$reportId"))
        UNUserNotificationCenter.currentNotificationCenter()
            .removeDeliveredNotificationsWithIdentifiers(listOf("sync_failure_$reportId"))
    }

    /**
     * Register the RETRY_SYNC notification action category.
     * Call this from the iOS app entry point (AppDelegate / MainViewController.swift) at startup.
     */
    fun registerNotificationCategory() {
        val retryLabel = runBlocking { getString(Res.string.sync_notification_action_retry) }
        val retryAction = UNNotificationAction.actionWithIdentifier(
            identifier = RETRY_SYNC_ACTION_ID,
            title = retryLabel,
            options = UNNotificationActionOptions.uNNotificationActionOptionNone
        )
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = SYNC_FAILURE_CATEGORY_ID,
            actions = listOf(retryAction),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptions.uNNotificationCategoryOptionNone
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .setNotificationCategories(setOf(category))
    }
}
