package com.espert.reporteciudadano.feature.reportsmap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.espert.reporteciudadano.feature.reportdetail.ReportDetailScreen
import com.espert.reporteciudadano.platform.MapView
import com.espert.reporteciudadano.ui.adaptive.NoSelectionPlaceholder
import com.espert.reporteciudadano.ui.adaptive.isExpandedWidth
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
    val expanded = isExpandedWidth()

    if (expanded) {
        ReportsMapTwoPane(
            state = state,
            onIntent = viewModel::processIntent
        )
    } else {
        ReportsMapSinglePane(
            state = state,
            onReportSelected = onReportSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsMapSinglePane(
    state: ReportsMapState,
    onReportSelected: (String) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            MapView(reports = state.pins, onMarkerClick = onReportSelected, modifier = Modifier.fillMaxSize().clipToBounds())
        }
        CenterAlignedTopAppBar(
            title = { Text(stringResource(Res.string.reports_map_title)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsMapTwoPane(
    state: ReportsMapState,
    onIntent: (ReportsMapIntent) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        // Left pane — map (weight 0.6f)
        Box(Modifier.weight(0.6f).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                MapView(
                    reports = state.pins,
                    onMarkerClick = { id -> onIntent(ReportsMapIntent.SelectPin(id)) },
                    modifier = Modifier.fillMaxSize().clipToBounds()
                )
            }
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.reports_map_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }

        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Right pane — detail or placeholder (weight 0.4f)
        Box(Modifier.weight(0.4f).fillMaxSize()) {
            val selectedId = state.selectedReportId
            if (selectedId != null) {
                ReportDetailScreen(
                    reportId = selectedId,
                    onBack = { onIntent(ReportsMapIntent.ClearSelection) },
                    showBackArrow = false
                )
            } else {
                NoSelectionPlaceholder()
            }
        }
    }
}
