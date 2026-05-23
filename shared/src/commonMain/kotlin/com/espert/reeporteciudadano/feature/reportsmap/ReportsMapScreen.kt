package com.espert.reeporteciudadano.feature.reportsmap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espert.reeporteciudadano.platform.MapView
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReportsMapScreen(
    onReportSelected: (String) -> Unit,
    viewModel: ReportsMapViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            MapView(reports = state.pins, onMarkerClick = onReportSelected, modifier = Modifier.fillMaxSize())
        }
        CenterAlignedTopAppBar(
            title = { Text("Reports Map") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            )
        )
    }
}
