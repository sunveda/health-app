import type { AppDependencies } from "../core/adapters";
import {
  displayAvailability,
  displayFeatureFlag,
  displayPipeline,
  displaySession,
  displaySync,
} from "../core/CapabilityStatus";

type TabId = "home" | "health" | "expenses" | "settings";

const TABS: { id: TabId; label: string }[] = [
  { id: "home", label: "Home" },
  { id: "health", label: "Health" },
  { id: "expenses", label: "Expenses" },
  { id: "settings", label: "Settings" },
];

export function PortalShell({
  dependencies,
  activeTab,
  onTabChange,
}: {
  dependencies: AppDependencies;
  activeTab: TabId;
  onTabChange: (tab: TabId) => void;
}) {
  return (
    <div className="portal">
      <header className="portal__header">
        <p className="portal__brand">My Health</p>
        <p className="portal__tagline">
          Web portal shell — identity and clinical uploads stay unavailable until
          approved.
        </p>
      </header>

      <nav className="portal__nav" aria-label="Primary">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={
              tab.id === activeTab
                ? "portal__nav-btn portal__nav-btn--active"
                : "portal__nav-btn"
            }
            onClick={() => onTabChange(tab.id)}
            aria-current={tab.id === activeTab ? "page" : undefined}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <main className="portal__main">
        {activeTab === "home" && <HomePanel dependencies={dependencies} />}
        {activeTab === "health" && <HealthPanel dependencies={dependencies} />}
        {activeTab === "expenses" && <ExpensesPanel />}
        {activeTab === "settings" && <SettingsPanel dependencies={dependencies} />}
      </main>
    </div>
  );
}

function HomePanel({ dependencies }: { dependencies: AppDependencies }) {
  return (
    <section className="panel">
      <h1>Today</h1>
      <dl className="status-list">
        <div>
          <dt>Identity session</dt>
          <dd>{displaySession(dependencies.identitySession.status)}</dd>
        </div>
        <div>
          <dt>Wellness sync</dt>
          <dd>{displaySync(dependencies.wellnessSync.status)}</dd>
        </div>
        <div>
          <dt>Clinical pipeline</dt>
          <dd>{displayPipeline(dependencies.clinicalPipeline.status)}</dd>
        </div>
      </dl>
      <p className="muted">
        No insights yet. Connect an approved source on a native client, or upload
        a report after the clinical path is reviewed.
      </p>
    </section>
  );
}

function HealthPanel({ dependencies }: { dependencies: AppDependencies }) {
  const snapshot = dependencies.healthDataSource.capabilitySnapshot();

  return (
    <section className="panel">
      <h1>Health</h1>
      <dl className="status-list">
        <div>
          <dt>Device health kits</dt>
          <dd>{displayAvailability(snapshot.availability)}</dd>
        </div>
        <div>
          <dt>Wellness feature flag</dt>
          <dd>{displayFeatureFlag(dependencies.wellnessSync.featureFlag)}</dd>
        </div>
        <div>
          <dt>Clinical pipeline</dt>
          <dd>{displayPipeline(dependencies.clinicalPipeline.status)}</dd>
        </div>
        <div>
          <dt>NFC / card reader</dt>
          <dd>{displayAvailability(dependencies.nfcCapability.status)}</dd>
        </div>
      </dl>
      <p className="muted">
        HealthKit and Health Connect are mobile-only. This web shell will not
        request device kit permissions.
      </p>
    </section>
  );
}

function ExpensesPanel() {
  return (
    <section className="panel">
      <h1>Expenses</h1>
      <p className="muted">
        Synthetic eligibility fixtures live in <code>packages/domain</code>.
        Native and web calculators are not wired; this is not tax-law guidance.
      </p>
    </section>
  );
}

function SettingsPanel({ dependencies }: { dependencies: AppDependencies }) {
  return (
    <section className="panel">
      <h1>Settings</h1>
      <dl className="status-list">
        <div>
          <dt>Secure store</dt>
          <dd>{displayAvailability(dependencies.secureStore.status)}</dd>
        </div>
        <div>
          <dt>Local unlock</dt>
          <dd>{displayAvailability(dependencies.localUnlock.status)}</dd>
        </div>
        <div>
          <dt>Consent store</dt>
          <dd>{displayAvailability(dependencies.consentStore.status)}</dd>
        </div>
        <div>
          <dt>Crash reporter</dt>
          <dd>{displayAvailability(dependencies.crashReporter.status)}</dd>
        </div>
        <div>
          <dt>Telemetry</dt>
          <dd>{displayAvailability(dependencies.telemetryPolicy.status)}</dd>
        </div>
      </dl>
      <p className="muted">
        Tokens must not be stored in <code>localStorage</code>. See{" "}
        <code>docs/web-baseline.md</code>.
      </p>
    </section>
  );
}

export type { TabId };
