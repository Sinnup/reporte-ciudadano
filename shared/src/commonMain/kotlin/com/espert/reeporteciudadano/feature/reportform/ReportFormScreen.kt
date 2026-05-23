package com.espert.reeporteciudadano.feature.reportform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.espert.reeporteciudadano.navigation.CapturedPhoto
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReportFormScreen(
    photos: List<CapturedPhoto>,
    onSubmitted: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ReportFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(photos) { viewModel.init(photos) }
    LaunchedEffect(state.submitted) { if (state.submitted) onSubmitted() }

    if (state.showDisclaimer) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("About the address") },
            text = { Text("The address shown may be approximate based on the photo's GPS coordinates.") },
            confirmButton = {
                Button(onClick = { viewModel.processIntent(ReportFormIntent.DismissDisclaimer) }) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Report") },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, "Cancel") } }
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = { viewModel.processIntent(ReportFormIntent.Submit) },
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSubmitting) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("Submit Report")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { photo ->
                    AsyncImage(
                        model = photo.localPath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.processIntent(ReportFormIntent.TitleChanged(it)) },
                    label = { Text("Title *") },
                    supportingText = { Text("${state.title.length}/100") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.processIntent(ReportFormIntent.DescriptionChanged(it)) },
                    label = { Text("Description *") },
                    supportingText = { Text("${state.description.length}/500") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        if (state.isLoadingAddress) CircularProgressIndicator(Modifier.size(20.dp))
                        else Text(state.address, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
