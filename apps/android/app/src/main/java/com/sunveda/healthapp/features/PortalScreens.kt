package com.sunveda.healthapp.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sunveda.healthapp.platform.PlatformDependencies
import com.sunveda.healthapp.platform.PrivacyCopyPlaceholder
import com.sunveda.healthapp.platform.WellnessCopyPlaceholder
import com.sunveda.healthapp.platform.WellnessPresentation
import com.sunveda.healthapp.platform.toDisplayLabel

@Composable
fun HomeScreen(dependencies: PlatformDependencies, modifier: Modifier = Modifier) {
    PortalPlaceholder(
        title = "Home",
        detail = "Identity session: ${dependencies.identitySession.status.toDisplayLabel()}\n" +
            "Wellness sync: ${dependencies.wellnessSync.status.toDisplayLabel()}",
        modifier = modifier,
    )
}

@Composable
fun HealthScreen(dependencies: PlatformDependencies, modifier: Modifier = Modifier) {
    val snapshot = dependencies.healthDataSource.capabilitySnapshot()
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Health", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Health data source: ${snapshot.availability.toDisplayLabel()}",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Permission: ${snapshot.permissionState.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Read scopes: ${WellnessPresentation.scopesLabel(snapshot.grantedReadScopes)}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Wellness sync: ${dependencies.wellnessSync.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Feature flag: ${dependencies.wellnessSync.featureFlag.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = WellnessCopyPlaceholder.KIT_WIRING_DEFERRED,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ExpensesScreen(modifier: Modifier = Modifier) {
    PortalPlaceholder(
        title = "Expenses",
        detail = "Connect an approved source to get started.",
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(dependencies: PlatformDependencies, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Application secure store: ${dependencies.secureStore.applicationStoreStatus.toDisplayLabel()}",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Biometric-gated store: ${dependencies.secureStore.biometricGatedStoreStatus.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Biometric unlock: ${dependencies.biometricUnlock.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "NFC: ${dependencies.nfcCapability.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Health data source: ${dependencies.healthDataSource.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Wellness sync: ${dependencies.wellnessSync.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Wellness feature flag: ${dependencies.wellnessSync.featureFlag.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = WellnessCopyPlaceholder.FEATURE_DISABLED,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = WellnessCopyPlaceholder.KIT_WIRING_DEFERRED,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "Consent store: ${dependencies.consentStore.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = PrivacyCopyPlaceholder.CONSENT_STORE_NOT_CONFIGURED,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "Crash reporting: ${dependencies.crashReporter.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Telemetry: ${dependencies.telemetryPolicy.status.toDisplayLabel()}",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = PrivacyCopyPlaceholder.CRASH_AND_TELEMETRY_DISABLED,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = PrivacyCopyPlaceholder.LEGAL_REVIEW_TODO,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PortalPlaceholder(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = detail,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
