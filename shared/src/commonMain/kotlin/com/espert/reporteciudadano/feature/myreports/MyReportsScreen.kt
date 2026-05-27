package com.espert.reporteciudadano.feature.myreports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reporteciudadano.domain.model.ReportStatus
import com.espert.reporteciudadano.domain.model.SyncStatus
import com.espert.reporteciudadano.feature.reportdetail.ReportDetailScreen
import com.espert.reporteciudadano.ui.adaptive.NoSelectionPlaceholder
import com.espert.reporteciudadano.ui.adaptive.isExpandedWidth
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    onReportSelected: (String) -> Unit,
    viewModel: MyReportsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val expanded = isExpandedWidth()

    if (expanded) {
        MyReportsTwoPane(
            state = state,
            onIntent = viewModel::processIntent
        )
    } else {
        MyReportsSinglePane(
            state = state,
            onReportSelected = onReportSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyReportsSinglePane(
    state: MyReportsState,
    onReportSelected: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.my_reports_title)) }, windowInsets = WindowInsets(0, 0, 0, 0)) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.reports.isEmpty() -> Text(
                    stringResource(Res.string.no_reports_message),
                    Modifier.align(Alignment.Center).padding(32.dp)
                )
                else -> ReportCardList(
                    state = state,
                    selectedReportId = null,
                    onCardClick = onReportSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyReportsTwoPane(
    state: MyReportsState,
    onIntent: (MyReportsIntent) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        // Left pane — report list
        Column(Modifier.weight(0.4f).fillMaxHeight()) {
            CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.my_reports_title)) }, windowInsets = WindowInsets(0, 0, 0, 0))
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.reports.isEmpty() -> Text(
                        stringResource(Res.string.no_reports_message),
                        Modifier.align(Alignment.Center).padding(32.dp)
                    )
                    else -> ReportCardList(
                        state = state,
                        selectedReportId = state.selectedReportId,
                        onCardClick = { id -> onIntent(MyReportsIntent.SelectReport(id)) }
                    )
                }
            }
        }

        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Right pane — detail or placeholder
        Box(Modifier.weight(0.6f).fillMaxHeight()) {
            val selectedId = state.selectedReportId
            if (selectedId != null) {
                ReportDetailScreen(
                    reportId = selectedId,
                    onBack = { onIntent(MyReportsIntent.ClearSelection) },
                    showBackArrow = false
                )
            } else {
                NoSelectionPlaceholder()
            }
        }
    }
}

@Composable
private fun ReportCardList(
    state: MyReportsState,
    selectedReportId: String?,
    onCardClick: (String) -> Unit
) {
    LazyColumn {
        items(state.reports) { report ->
            val isSelected = selectedReportId == report.id
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onCardClick(report.id) },
                colors = if (isSelected) {
                    CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                } else {
                    CardDefaults.elevatedCardColors()
                }
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        report.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    SyncStatusIcon(
                        syncStatus = state.syncStates[report.id] ?: SyncStatus.PENDING,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    StatusChip(report.status)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: ReportStatus) {
    val containerColor = when (status) {
        ReportStatus.SENT -> MaterialTheme.colorScheme.tertiaryContainer
        ReportStatus.SEEN -> MaterialTheme.colorScheme.secondaryContainer
        ReportStatus.PENDING -> MaterialTheme.colorScheme.errorContainer
        ReportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
        ReportStatus.RESOLVED -> MaterialTheme.colorScheme.surfaceVariant
        ReportStatus.DISCARDED -> MaterialTheme.colorScheme.surfaceVariant
    }
    SuggestionChip(
        onClick = {},
        label = { Text(statusLabel(status)) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = containerColor)
    )
}

@Composable
private fun statusLabel(status: ReportStatus): String = when (status) {
    ReportStatus.SENT        -> stringResource(Res.string.status_sent)
    ReportStatus.SEEN        -> stringResource(Res.string.status_seen)
    ReportStatus.PENDING     -> stringResource(Res.string.status_pending)
    ReportStatus.IN_PROGRESS -> stringResource(Res.string.status_in_progress)
    ReportStatus.RESOLVED    -> stringResource(Res.string.status_resolved)
    ReportStatus.DISCARDED   -> stringResource(Res.string.status_discarded)
}

/**
 * Displays a small cloud sync status icon (18dp) for a single report card row.
 *
 * SYNCED    → CloudDone, primary color
 * PENDING / IN_PROGRESS → CloudUpload, onSurfaceVariant (muted)
 * FAILED    → SyncProblem, error color (red)
 */
@Composable
fun SyncStatusIcon(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, tint, contentDesc) = when (syncStatus) {
        SyncStatus.SYNCED ->
            Triple(
                Icons.Default.CloudDone,
                MaterialTheme.colorScheme.primary,
                stringResource(Res.string.sync_status_synced_cd)
            )
        SyncStatus.PENDING,
        SyncStatus.IN_PROGRESS ->
            Triple(
                Icons.Default.CloudUpload,
                MaterialTheme.colorScheme.onSurfaceVariant,
                stringResource(Res.string.sync_status_pending_cd)
            )
        SyncStatus.FAILED ->
            Triple(
                Icons.Default.SyncProblem,
                MaterialTheme.colorScheme.error,
                stringResource(Res.string.sync_status_failed_cd)
            )
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDesc,
        tint = tint,
        modifier = modifier.size(18.dp)
    )
}
