package com.espert.reporteciudadano.feature.reportform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.espert.reporteciudadano.navigation.CapturedPhoto
import com.espert.reporteciudadano.ui.components.LocationDisplayCard
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    photos: List<CapturedPhoto>,
    onSubmitted: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ReportFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(photos) { viewModel.init(photos) }
    LaunchedEffect(Unit) { viewModel.submitted.collect { onSubmitted() } }

    if (state.showDisclaimer) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.address_dialog_title)) },
            text = { Text(stringResource(Res.string.address_dialog_body)) },
            confirmButton = {
                Button(onClick = { viewModel.processIntent(ReportFormIntent.DismissDisclaimer) }) {
                    Text(stringResource(Res.string.got_it_button))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.new_report_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, stringResource(Res.string.cancel_content_description))
                    }
                }
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
                    else Text(stringResource(Res.string.submit_report_button))
                }
            }
        }
    ) { padding ->
        // On wide viewports, cap the content column at 600dp and centre it.
        // The Scaffold fills the full width; only the inner column is constrained.
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
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
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { viewModel.processIntent(ReportFormIntent.TitleChanged(it)) },
                        label = { Text(stringResource(Res.string.title_field_label)) },
                        supportingText = { Text("${state.title.length}/100") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.processIntent(ReportFormIntent.DescriptionChanged(it)) },
                        label = { Text(stringResource(Res.string.description_field_label)) },
                        supportingText = { Text("${state.description.length}/500") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LocationDisplayCard(
                        locationDisplay = state.locationDisplay,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
