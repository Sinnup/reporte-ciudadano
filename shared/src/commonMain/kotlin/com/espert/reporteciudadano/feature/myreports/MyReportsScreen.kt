package com.espert.reporteciudadano.feature.myreports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reporteciudadano.domain.model.ReportStatus
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

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.my_reports_title)) }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.reports.isEmpty() -> Text(
                    stringResource(Res.string.no_reports_message),
                    Modifier.align(Alignment.Center).padding(32.dp)
                )
                else -> LazyColumn {
                    items(state.reports) { report ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onReportSelected(report.id) }
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(report.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                StatusChip(report.status)
                            }
                        }
                    }
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
