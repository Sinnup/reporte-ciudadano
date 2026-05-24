package com.espert.reporteciudadano.feature.reportsmap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.espert.reporteciudadano.platform.MapView
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            title = { Text(stringResource(Res.string.reports_map_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                scrolledContainerColor = Color.Unspecified,
                navigationIconContentColor = Color.Unspecified,
                titleContentColor = Color.Unspecified,
                actionIconContentColor = Color.Unspecified
            )
        )
    }
}
