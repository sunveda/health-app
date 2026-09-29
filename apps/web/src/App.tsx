import { useState } from "react";
import { createCompositionRoot } from "./core/CompositionRoot";
import { PortalShell, type TabId } from "./features/PortalScreens";

const dependencies = createCompositionRoot();

export default function App() {
  const [activeTab, setActiveTab] = useState<TabId>("home");

  return (
    <PortalShell
      dependencies={dependencies}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    />
  );
}
