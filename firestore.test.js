const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_safety_01";
const BOB_UID = "bob_safety_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot list or write hazards, capas, or audits", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/hazards`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/capas`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/audits`).get());
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_1").set({
      userId: ALICE_UID,
      code: "NM-2026-001",
      title: "Unauth write",
      description: "Attempted unauthenticated hazard",
      category: "NEAR_MISS",
      severity: "HIGH",
      status: "OPEN",
      location: "Sector A",
      likelihood: 4,
      impact: 4,
      riskScore: 16,
      ppeChecklist: ["Hard Hat"],
      stopWorkIssued: false,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can list empty collections and perform valid Kotlin-mirrored hazard lifecycle", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Empty list query succeeds
  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/hazards`).get());

  // Valid create (no @DocumentId `id` key, optional evidenceUri omitted)
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_001").set({
      userId: ALICE_UID,
      code: "NM-2026-089",
      title: "High-pressure steam flange seepage",
      description: "Visible condensation spray near valve V-402.",
      category: "UNSAFE_CONDITION",
      severity: "CRITICAL",
      status: "STOP_WORK",
      location: "Sector B • Compressor Bay 4",
      likelihood: 4,
      impact: 5,
      riskScore: 20,
      ppeChecklist: ["Hard Hat", "Safety Glasses", "Face Shield"],
      stopWorkIssued: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid partial status update to IN_PROGRESS
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_001").update({
      status: "IN_PROGRESS",
      stopWorkIssued: false,
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Valid resolve update
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_001").update({
      status: "RESOLVED",
      stopWorkIssued: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: RESOLVED hazard cannot be mutated further
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_001").update({
      status: "OPEN",
      stopWorkIssued: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Hazard rules reject shadow fields, @DocumentId id field, mismatched riskScore, and future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Reject extra @DocumentId 'id' field
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_bad_id").set({
      id: "haz_bad_id",
      userId: ALICE_UID,
      code: "NM-2026-090",
      title: "Invalid ID field",
      description: "Contains id field",
      category: "NEAR_MISS",
      severity: "LOW",
      status: "OPEN",
      location: "Dock 1",
      likelihood: 2,
      impact: 2,
      riskScore: 4,
      ppeChecklist: [],
      stopWorkIssued: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject inconsistent riskScore != likelihood * impact
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_bad_score").set({
      userId: ALICE_UID,
      code: "NM-2026-091",
      title: "Tampered risk score",
      description: "Risk score does not match matrix",
      category: "NEAR_MISS",
      severity: "LOW",
      status: "OPEN",
      location: "Dock 1",
      likelihood: 4,
      impact: 4,
      riskScore: 1,
      ppeChecklist: [],
      stopWorkIssued: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/hazards`).doc("haz_future").set({
      userId: ALICE_UID,
      code: "NM-2026-092",
      title: "Future timestamp",
      description: "Tampered clock",
      category: "NEAR_MISS",
      severity: "LOW",
      status: "OPEN",
      location: "Dock 1",
      likelihood: 2,
      impact: 2,
      riskScore: 4,
      ppeChecklist: [],
      stopWorkIssued: false,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );
});

test("CAPA milestone progression and Audit checklist updates work for owner and block cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 8000);

  // Create CAPA
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/capas`).doc("capa_001").set({
      userId: ALICE_UID,
      code: "CAPA-2026-041",
      title: "Replace flange gasket & install pressure relief interlock",
      linkedHazardCode: "NM-2026-089",
      ownerName: "M. Vance",
      dueDateLabel: "Nov 24, 2026",
      priority: "CRITICAL",
      status: "IN_PROGRESS",
      currentStep: 1,
      rootCauseSummary: "Thermal fatigue on Grade-B gasket.",
      overdue: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Advance CAPA step
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/capas`).doc("capa_001").update({
      currentStep: 2,
      status: "VERIFICATION",
      overdue: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject stepping backwards
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/capas`).doc("capa_001").update({
      currentStep: 0,
      status: "OPEN",
      overdue: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Create Audit
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/audits`).doc("aud_001").set({
      userId: ALICE_UID,
      code: "AUD-2026-019",
      title: "Confined Space & LOTO Verification",
      zone: "Tank Farm North",
      inspectorName: "R. Chen",
      status: "IN_PROGRESS",
      passedCount: 1,
      totalCount: 2,
      checklistLabels: ["Zero-energy verification tag applied", "Atmospheric O2 > 19.5% logged"],
      checklistStates: ["PASS", "PENDING"],
      notes: "Initial lock verification complete.",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Complete Audit
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/audits`).doc("aud_001").update({
      checklistStates: ["PASS", "PASS"],
      passedCount: 2,
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation: Bob cannot read or mutate Alice's records
  await assertFails(bobDb.collection(`users/${ALICE_UID}/hazards`).get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/capas`).doc("capa_001").get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/audits`).doc("aud_001").get());
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/capas`).doc("capa_001").update({
      currentStep: 3,
      status: "CLOSED",
      overdue: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("OperatorProfile registration, partial update, and PII isolation rules", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Owner can create their OperatorProfile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Vance",
      badgeId: "EHS-0412",
      role: "EHS_SUPERVISOR",
      facilityZone: "Plant-04 • Sector B",
      email: "alice@example.com",
      accessActive: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Owner can update role/facilityZone without modifying createdAt or accessActive
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Alice Vance",
      badgeId: "EHS-0412",
      role: "PLANT_MANAGER",
      facilityZone: "Plant-04 • All Sectors",
      email: "alice@example.com",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject cross-user PII read or tampering
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      role: "PLANT_MANAGER",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
