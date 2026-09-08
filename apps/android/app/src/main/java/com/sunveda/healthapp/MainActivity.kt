package com.sunveda.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sunveda.healthapp.core.HealthAppTheme
import com.sunveda.healthapp.features.ExpensesScreen
import com.sunveda.healthapp.features.HealthScreen
import com.sunveda.healthapp.features.HomeScreen
import com.sunveda.healthapp.features.SettingsScreen
import com.sunveda.healthapp.platform.CompositionRoot
import com.sunveda.healthapp.platform.PlatformDependencies

class MainActivity : ComponentActivity() {
    private val dependencies = CompositionRoot.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthAppTheme {
                HealthApp(dependencies)
            }
        }
    }
}

@Composable
internal fun HealthApp(dependencies: PlatformDependencies) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Home", "Health", "Expenses", "Settings")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        }
    ) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        when (selectedTab) {
            0 -> HomeScreen(dependencies, modifier)
            1 -> HealthScreen(dependencies, modifier)
            2 -> ExpensesScreen(modifier)
            else -> SettingsScreen(dependencies, modifier)
        }
    }
}
