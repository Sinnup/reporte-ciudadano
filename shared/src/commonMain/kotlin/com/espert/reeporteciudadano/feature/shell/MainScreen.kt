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
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Report") },
                    label = { Text("Report") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.MY_REPORTS,
                    onClick = { onTabSelected(BottomTab.MY_REPORTS) },
                    icon = { Icon(Icons.Default.List, contentDescription = "My Reports") },
                    label = { Text("My Reports") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.MAP,
                    onClick = { onTabSelected(BottomTab.MAP) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") }
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
            Text("Report a Pothole", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onRegisterPothole) { Text("Register pothole") }
        }
    }
}
