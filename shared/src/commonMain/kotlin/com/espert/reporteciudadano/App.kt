package com.espert.reporteciudadano

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.espert.reporteciudadano.feature.camera.CameraScreen
import com.espert.reporteciudadano.feature.camera.CameraViewModel
import com.espert.reporteciudadano.feature.camera.PhotoReviewScreen
import com.espert.reporteciudadano.feature.myreports.MyReportsIntent
import com.espert.reporteciudadano.feature.myreports.MyReportsViewModel
import com.espert.reporteciudadano.feature.reportdetail.ReportDetailScreen
import com.espert.reporteciudadano.feature.reportform.ReportFormScreen
import com.espert.reporteciudadano.feature.reportsmap.ReportsMapIntent
import com.espert.reporteciudadano.feature.reportsmap.ReportsMapViewModel
import com.espert.reporteciudadano.feature.shell.MainScreen
import com.espert.reporteciudadano.feature.thankyou.ThankYouScreen
import com.espert.reporteciudadano.navigation.*
import com.espert.reporteciudadano.ui.adaptive.isExpandedWidth
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            val appViewModel: AppViewModel = koinViewModel()
            val destination by appViewModel.currentDestination.collectAsState()
            val selectedTab by appViewModel.selectedTab.collectAsState()

            // Obtain ViewModels at App level so they can be accessed for clearing selection
            val myReportsViewModel: MyReportsViewModel = koinViewModel()
            val reportsMapViewModel: ReportsMapViewModel = koinViewModel()

            val expanded = isExpandedWidth()

            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                when (val dest = destination) {
                is NavDestination.MainShell -> MainScreen(
                    selectedTab = selectedTab,
                    onTabSelected = appViewModel::selectTab,
                    onRegisterPothole = { appViewModel.navigate(NavDestination.Camera) },
                    onReportSelected = { id ->
                        if (expanded) {
                            // On expanded width, the individual screens handle selection via their ViewModels.
                            // This callback is a no-op here; MyReportsScreen and ReportsMapScreen dispatch their own intents.
                        } else {
                            appViewModel.navigate(NavDestination.ReportDetail(id))
                        }
                    },
                    onClearMyReportsSelection = {
                        myReportsViewModel.processIntent(MyReportsIntent.ClearSelection)
                    },
                    onClearMapSelection = {
                        reportsMapViewModel.processIntent(ReportsMapIntent.ClearSelection)
                    }
                )
                is NavDestination.Camera -> CameraScreen(
                    onPhotosReady = { photos -> appViewModel.navigate(NavDestination.PhotoReview(photos)) },
                    onCancel = { appViewModel.back() }
                )
                is NavDestination.PhotoReview -> {
                    val cameraViewModel: CameraViewModel = koinViewModel()
                    val cameraState by cameraViewModel.state.collectAsState()
                    PhotoReviewScreen(
                        photos = dest.photos,
                        noLocationOnPhotos = cameraState.noLocationOnPhotos,
                        onContinue = { appViewModel.navigate(NavDestination.ReportForm(dest.photos)) },
                        onIntent = cameraViewModel::processIntent,
                        onCancel = { appViewModel.backToMain() }
                    )
                }
                is NavDestination.ReportForm -> ReportFormScreen(
                    photos = dest.photos,
                    onSubmitted = { appViewModel.navigate(NavDestination.ThankYou) },
                    onCancel = { appViewModel.backToMain() }
                )
                is NavDestination.ThankYou -> ThankYouScreen(
                    onDone = { appViewModel.backToMain() }
                )
                is NavDestination.ReportDetail -> ReportDetailScreen(
                    reportId = dest.reportId,
                    onBack = { appViewModel.back() }
                )
            }
        }
    }
}
}
