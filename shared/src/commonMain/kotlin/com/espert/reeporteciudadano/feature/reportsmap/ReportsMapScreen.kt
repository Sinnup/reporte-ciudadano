package com.espert.reeporteciudadano.feature.reportsmap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espert.reeporteciudadano.platform.MapView
import org.jetbrains.compose.resources.stringResource
import reeporteciudadano.shared.generated.resources.Res
import reeporteciudadano.shared.generated.resources.*
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
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            )
        )
    }
}
