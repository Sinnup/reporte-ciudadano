package com.espert.reeporteciudadano.feature.myreports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reeporteciudadano.domain.model.ReportStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyReportsScreen(
    onReportSelected: (String) -> Unit,
    viewModel: MyReportsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Reports") }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.reports.isEmpty() -> Text(
                    "No reports yet. Tap 'Report' to submit your first one.",
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
        label = { Text(status.name.replace("_", " ")) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = containerColor)
    )
}
