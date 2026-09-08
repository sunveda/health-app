import SwiftUI

struct DashboardView: View {
    let dependencies: AppDependencies

    var body: some View {
        NavigationStack {
            List {
                Section("Today") {
                    Label("Connect your verified identity", systemImage: "person.badge.key")
                    LabeledContent("Identity session") {
                        Text(displayLabel(for: dependencies.identitySession.status))
                            .foregroundStyle(.secondary)
                    }
                    Label("Review wellness sync status", systemImage: "arrow.triangle.2.circlepath")
                }

                Section("Insights") {
                    ContentUnavailableView(
                        "No insights yet",
                        systemImage: "sparkles",
                        description: Text("Add an approved health source or report to receive explainable summaries.")
                    )
                }
            }
            .navigationTitle("My Health")
        }
    }
}

struct HealthView: View {
    let dependencies: AppDependencies

    var body: some View {
        NavigationStack {
            List {
                Section("Connected sources") {
                    HealthSourceRow(
                        title: "Apple Health",
                        detail: displayLabel(for: dependencies.healthDataSource.status),
                        symbol: "heart.fill"
                    )
                    HealthSourceRow(title: "Medical records", detail: "Not connected", symbol: "cross.case.fill")
                }
            }
            .navigationTitle("Health")
        }
    }
}

struct ExpensesView: View {
    var body: some View {
        NavigationStack {
            ContentUnavailableView(
                "No medical expenses imported",
                systemImage: "yensign.circle",
                description: Text("Connect an approved source to review eligible expenses and prepare an export.")
            )
            .navigationTitle("Expenses")
        }
    }
}

struct SettingsView: View {
    let dependencies: AppDependencies

    var body: some View {
        NavigationStack {
            List {
                Section("Privacy") {
                    Label("Consent and connected sources", systemImage: "checkmark.shield")
                    Label("Data export and deletion", systemImage: "arrow.down.doc")
                }

                Section("Account") {
                    LabeledContent("Identity verification") {
                        Text(displayLabel(for: dependencies.identitySession.status))
                            .foregroundStyle(.secondary)
                    }
                    LabeledContent("Biometric unlock") {
                        Text(displayLabel(for: dependencies.biometricUnlock.status))
                            .foregroundStyle(.secondary)
                    }
                }

                Section("Platform capabilities") {
                    LabeledContent("Application secure store") {
                        Text(displayLabel(for: dependencies.secureStore.applicationStoreStatus))
                            .foregroundStyle(.secondary)
                    }
                    LabeledContent("Biometric-gated store") {
                        Text(displayLabel(for: dependencies.secureStore.biometricGatedStoreStatus))
                            .foregroundStyle(.secondary)
                    }
                    LabeledContent("NFC") {
                        Text(displayLabel(for: dependencies.nfcCapability.status))
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}

private struct HealthSourceRow: View {
    let title: String
    let detail: String
    let symbol: String

    var body: some View {
        Label {
            VStack(alignment: .leading) {
                Text(title)
                Text(detail)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        } icon: {
            Image(systemName: symbol)
                .foregroundStyle(.teal)
        }
    }
}
