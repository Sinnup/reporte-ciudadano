package com.espert.reporteciudadano.feature.thankyou

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*

@Composable
fun ThankYouScreen(onDone: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(4000))

    LaunchedEffect(Unit) {
        progress = 1f
        delay(4000)
        onDone()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Text(stringResource(Res.string.thank_you_headline), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(Res.string.thank_you_body),
                style = MaterialTheme.typography.bodyLarge
            )
            LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth())
        }
    }
}
