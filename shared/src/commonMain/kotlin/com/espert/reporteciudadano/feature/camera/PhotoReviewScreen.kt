package com.espert.reporteciudadano.feature.camera

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.espert.reporteciudadano.navigation.CapturedPhoto
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoReviewScreen(
    photos: List<CapturedPhoto>,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.review_photos_title)) },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, stringResource(Res.string.cancel_content_description)) } }
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = onContinue,
                    enabled = photos.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.continue_button))
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (photos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.no_photos_message))
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photo ->
                        Box {
                            AsyncImage(
                                model = photo.localPath,
                                contentDescription = stringResource(Res.string.photo_content_description),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
