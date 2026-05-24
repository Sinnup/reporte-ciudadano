package com.espert.reeporteciudadano

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.espert.reeporteciudadano.feature.camera.CameraScreen
import com.espert.reeporteciudadano.feature.camera.PhotoReviewScreen
import com.espert.reeporteciudadano.feature.reportdetail.ReportDetailScreen
import com.espert.reeporteciudadano.feature.reportform.ReportFormScreen
import com.espert.reeporteciudadano.feature.shell.MainScreen
import com.espert.reeporteciudadano.feature.thankyou.ThankYouScreen
import com.espert.reeporteciudadano.navigation.*
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
                is NavDestination.PhotoReview -> PhotoReviewScreen(
                    photos = dest.photos,
                    onContinue = { appViewModel.navigate(NavDestination.ReportForm(dest.photos)) },
                    onCancel = { appViewModel.backToMain() }
                )
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
