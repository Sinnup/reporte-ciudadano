package com.espert.reeporteciudadano.feature.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reeporteciudadano.navigation.BottomTab
import org.jetbrains.compose.resources.stringResource
import reeporteciudadano.shared.generated.resources.Res
import reeporteciudadano.shared.generated.resources.*

@Composable
fun MainScreen(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onRegisterPothole: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.REPORT,
                    onClick = { onTabSelected(BottomTab.REPORT) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = stringResource(Res.string.tab_report)) },
                    label = { Text(stringResource(Res.string.tab_report)) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.MY_REPORTS,
                    onClick = { onTabSelected(BottomTab.MY_REPORTS) },
                    icon = { Icon(Icons.Default.List, contentDescription = stringResource(Res.string.tab_my_reports)) },
                    label = { Text(stringResource(Res.string.tab_my_reports)) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.MAP,
                    onClick = { onTabSelected(BottomTab.MAP) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(Res.string.tab_map)) },
                    label = { Text(stringResource(Res.string.tab_map)) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                BottomTab.REPORT -> ReportTabContent(onRegisterPothole = onRegisterPothole)
                BottomTab.MY_REPORTS -> com.espert.reeporteciudadano.feature.myreports.MyReportsScreen(
                    onReportSelected = onReportSelected
                )
                BottomTab.MAP -> com.espert.reeporteciudadano.feature.reportsmap.ReportsMapScreen(
                    onReportSelected = onReportSelected
                )
            }
        }
    }
}

@Composable
private fun ReportTabContent(onRegisterPothole: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(Res.string.report_tab_headline), style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onRegisterPothole) { Text(stringResource(Res.string.register_pothole_button)) }
        }
    }
}
