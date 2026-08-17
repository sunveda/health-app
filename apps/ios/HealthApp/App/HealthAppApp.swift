import SwiftUI

@main
struct HealthAppApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}

struct RootView: View {
    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Label("Home", systemImage: "heart.text.square")
                }

            HealthView()
                .tabItem {
                    Label("Health", systemImage: "waveform.path.ecg")
                }

            ExpensesView()
                .tabItem {
                    Label("Expenses", systemImage: "yensign.circle")
                }

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
        }
        .tint(.teal)
    }
}

#Preview {
    RootView()
}
