import SwiftUI

@main
struct HealthAppApp: App {
    private let dependencies = CompositionRoot.make()

    var body: some Scene {
        WindowGroup {
            RootView(dependencies: dependencies)
        }
    }
}

struct RootView: View {
    let dependencies: AppDependencies

    var body: some View {
        TabView {
            DashboardView(dependencies: dependencies)
                .tabItem {
                    Label("Home", systemImage: "heart.text.square")
                }

            HealthView(dependencies: dependencies)
                .tabItem {
                    Label("Health", systemImage: "waveform.path.ecg")
                }

            ExpensesView()
                .tabItem {
                    Label("Expenses", systemImage: "yensign.circle")
                }

            SettingsView(dependencies: dependencies)
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
        }
        .tint(.teal)
    }
}

#Preview {
    RootView(dependencies: CompositionRoot.make())
}
