package com.espert.reporteciudadano.feature.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espert.reporteciudadano.navigation.BottomTab
import com.espert.reporteciudadano.ui.adaptive.isMediumOrLargerWidth
import org.jetbrains.compose.resources.stringResource
import reporteciudadano.shared.generated.resources.Res
import reporteciudadano.shared.generated.resources.*

@Composable
fun MainScreen(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onRegisterPothole: () -> Unit,
    onReportSelected: (String) -> Unit,
    onClearMyReportsSelection: () -> Unit = {},
    onClearMapSelection: () -> Unit = {}
) {
    val useRail = isMediumOrLargerWidth()

    // Dispatch ClearSelection when leaving My Reports or Map tabs in two-pane mode
    LaunchedEffect(selectedTab) {
        if (selectedTab != BottomTab.MY_REPORTS) onClearMyReportsSelection()
        if (selectedTab != BottomTab.MAP) onClearMapSelection()
    }

    if (useRail) {
        MainScreenWithRail(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onRegisterPothole = onRegisterPothole,
            onReportSelected = onReportSelected
        )
    } else {
        MainScreenWithBottomBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onRegisterPothole = onRegisterPothole,
            onReportSelected = onReportSelected
        )
    }
}

@Composable
private fun MainScreenWithBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onRegisterPothole: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.REPORT,
                    onClick = { onTabSelected(BottomTab.REPORT) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = stringResource(Res.string.tab_report)) },
                    label = { Text(stringResource(Res.string.tab_report)) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.MY_REPORTS,
                    onClick = { onTabSelected(BottomTab.MY_REPORTS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.tab_my_reports)) },
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
            TabContent(
                selectedTab = selectedTab,
                onRegisterPothole = onRegisterPothole,
                onReportSelected = onReportSelected
            )
        }
    }
}

@Composable
private fun MainScreenWithRail(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onRegisterPothole: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            NavigationRailItem(
                selected = selectedTab == BottomTab.REPORT,
                onClick = { onTabSelected(BottomTab.REPORT) },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = stringResource(Res.string.tab_report)) },
                label = { Text(stringResource(Res.string.tab_report)) },
                alwaysShowLabel = true
            )
            NavigationRailItem(
                selected = selectedTab == BottomTab.MY_REPORTS,
                onClick = { onTabSelected(BottomTab.MY_REPORTS) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.tab_my_reports)) },
                label = { Text(stringResource(Res.string.tab_my_reports)) },
                alwaysShowLabel = true
            )
            NavigationRailItem(
                selected = selectedTab == BottomTab.MAP,
                onClick = { onTabSelected(BottomTab.MAP) },
                icon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(Res.string.tab_map)) },
                label = { Text(stringResource(Res.string.tab_map)) },
                alwaysShowLabel = true
            )
        }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            TabContent(
                selectedTab = selectedTab,
                onRegisterPothole = onRegisterPothole,
                onReportSelected = onReportSelected
            )
        }
    }
}

@Composable
private fun TabContent(
    selectedTab: BottomTab,
    onRegisterPothole: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    when (selectedTab) {
        BottomTab.REPORT -> ReportTabContent(onRegisterPothole = onRegisterPothole)
        BottomTab.MY_REPORTS -> com.espert.reporteciudadano.feature.myreports.MyReportsScreen(
            onReportSelected = onReportSelected
        )
        BottomTab.MAP -> com.espert.reporteciudadano.feature.reportsmap.ReportsMapScreen(
            onReportSelected = onReportSelected
        )
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
