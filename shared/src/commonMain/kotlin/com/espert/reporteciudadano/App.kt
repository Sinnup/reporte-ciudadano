package com.espert.reporteciudadano

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
import com.espert.reporteciudadano.feature.reportdetail.ReportDetailScreen
import com.espert.reporteciudadano.feature.reportform.ReportFormScreen
import com.espert.reporteciudadano.feature.shell.MainScreen
import com.espert.reporteciudadano.feature.thankyou.ThankYouScreen
import com.espert.reporteciudadano.navigation.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val appViewModel: AppViewModel = koinViewModel()
            val destination by appViewModel.currentDestination.collectAsState()
            val selectedTab by appViewModel.selectedTab.collectAsState()

            when (val dest = destination) {
                is NavDestination.MainShell -> MainScreen(
                    selectedTab = selectedTab,
                    onTabSelected = appViewModel::selectTab,
                    onRegisterPothole = { appViewModel.navigate(NavDestination.Camera) },
                    onReportSelected = { id -> appViewModel.navigate(NavDestination.ReportDetail(id)) }
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
