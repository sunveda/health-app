import { describe, expect, it } from "vitest";
import { createCompositionRoot } from "./CompositionRoot";

describe("createCompositionRoot", () => {
  it("fails closed: identity, clinical, and kits are not live", () => {
    const deps = createCompositionRoot();

    expect(deps.identitySession.status).toBe("not_configured");
    expect(deps.clinicalPipeline.status).toBe("not_configured");
    expect(deps.wellnessSync.featureFlag).toBe("disabled");
    expect(deps.healthDataSource.capabilitySnapshot().availability).toBe(
      "unavailable",
    );
    expect(deps.nfcCapability.status).toBe("unavailable");
    expect(deps.secureStore.status).toBe("not_configured");
  });
});
