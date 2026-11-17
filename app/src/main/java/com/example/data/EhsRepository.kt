package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EhsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    fun observeOperatorProfile(userId: String): Flow<OperatorProfile?> = flow {
        requireUserId(userId)
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            OperatorProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun upsertOperatorProfile(
        userId: String,
        fullName: String,
        badgeId: String,
        role: AccessRole,
        facilityZone: String,
        email: String,
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanName = fullName.trim().take(80)
            val cleanBadge = badgeId.trim().uppercase().take(32)
            val cleanZone = facilityZone.trim().take(80)
            val cleanEmail = email.trim().take(120)
            require(cleanName.length in 2..80) { "Operator name must be 2–80 characters." }
            require(cleanBadge.length in 2..32) { "Badge ID must be 2–32 characters." }
            require(cleanZone.length in 2..80) { "Facility zone must be 2–80 characters." }

            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "fullName" to cleanName,
                        "badgeId" to cleanBadge,
                        "role" to role.name,
                        "facilityZone" to cleanZone,
                        "email" to cleanEmail,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "fullName" to cleanName,
                        "badgeId" to cleanBadge,
                        "role" to role.name,
                        "facilityZone" to cleanZone,
                        "email" to cleanEmail,
                        "accessActive" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeHazards(userId: String): Flow<List<HazardReport>> = flow {
        requireUserId(userId)
        val path = "users/$userId/hazards"
        emitAll(
            db.collection("users").document(userId).collection("hazards")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        HazardReport::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCapas(userId: String): Flow<List<CapaAction>> = flow {
        requireUserId(userId)
        val path = "users/$userId/capas"
        emitAll(
            db.collection("users").document(userId).collection("capas")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CapaAction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeAudits(userId: String): Flow<List<SafetyAudit>> = flow {
        requireUserId(userId)
        val path = "users/$userId/audits"
        emitAll(
            db.collection("users").document(userId).collection("audits")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SafetyAudit::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createHazard(
        userId: String,
        title: String,
        description: String,
        category: HazardCategory,
        location: String,
        likelihood: Int,
        impact: Int,
        ppeChecklist: List<String>,
        stopWorkIssued: Boolean,
        evidenceUri: String?,
        customCode: String? = null,
    ): Result<Unit> {
        val docId = "haz_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$userId/hazards/$docId"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim()
            val cleanDesc = description.trim()
            val cleanLocation = location.trim()
            require(cleanTitle.length in 3..140) { "Hazard title must be 3–140 characters." }
            require(cleanDesc.length in 1..1000) { "Observation details must be 1–1000 characters." }
            require(cleanLocation.length in 2..120) { "Location must be 2–120 characters." }
            require(likelihood in 1..5 && impact in 1..5) { "Risk matrix values must be 1–5." }

            val score = likelihood * impact
            val severity = if (stopWorkIssued) SeverityLevel.CRITICAL else SeverityLevel.fromRiskScore(score)
            val status = if (stopWorkIssued) HazardStatus.STOP_WORK else HazardStatus.OPEN
            val code = (customCode?.trim()?.takeIf { it.length in 3..32 })
                ?: "${category.codePrefix}-2026-${(100..999).random()}"

            val cleanEvidence = evidenceUri?.trim()?.takeIf { it.isNotEmpty() && it.length <= 500 }

            val payload = mapOf(
                "userId" to uid,
                "code" to code,
                "title" to cleanTitle,
                "description" to cleanDesc,
                "category" to category.name,
                "severity" to severity.name,
                "status" to status.name,
                "location" to cleanLocation,
                "likelihood" to likelihood.toLong(),
                "impact" to impact.toLong(),
                "riskScore" to score.toLong(),
                "ppeChecklist" to ppeChecklist.take(10),
                "stopWorkIssued" to stopWorkIssued,
                "evidenceUri" to cleanEvidence,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            db.collection("users").document(uid).collection("hazards").document(docId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateHazardStatus(
        userId: String,
        hazardId: String,
        newStatus: HazardStatus,
        stopWorkIssued: Boolean,
    ): Result<Unit> {
        val path = "users/$userId/hazards/$hazardId"
        return try {
            val uid = requireUserId(userId)
            require(hazardId.isNotBlank()) { "Hazard ID must not be blank." }
            db.collection("users").document(uid).collection("hazards").document(hazardId)
                .update(
                    mapOf(
                        "status" to newStatus.name,
                        "stopWorkIssued" to stopWorkIssued,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteHazard(userId: String, hazardId: String): Result<Unit> {
        val path = "users/$userId/hazards/$hazardId"
        return try {
            val uid = requireUserId(userId)
            require(hazardId.isNotBlank()) { "Hazard ID must not be blank." }
            db.collection("users").document(uid).collection("hazards").document(hazardId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun createCapa(
        userId: String,
        title: String,
        linkedHazardCode: String?,
        ownerName: String,
        dueDateLabel: String,
        priority: SeverityLevel,
        rootCauseSummary: String,
        overdue: Boolean = false,
        initialStep: Int = 0,
        customCode: String? = null,
    ): Result<Unit> {
        val docId = "capa_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$userId/capas/$docId"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim()
            val cleanOwner = ownerName.trim()
            val cleanDue = dueDateLabel.trim()
            val cleanRoot = rootCauseSummary.trim().take(600)
            require(cleanTitle.length in 3..140) { "CAPA title must be 3–140 characters." }
            require(cleanOwner.length in 2..80) { "Owner name must be 2–80 characters." }
            require(cleanDue.length in 2..32) { "Due date must be 2–32 characters." }
            val step = initialStep.coerceIn(0, 3)
            val status = CapaStatus.fromStep(step)
            val code = (customCode?.trim()?.takeIf { it.length in 3..32 })
                ?: "CAPA-2026-${(100..999).random()}"
            val cleanLinked = linkedHazardCode?.trim()?.takeIf { it.isNotEmpty() && it.length <= 32 }

            val payload = mapOf(
                "userId" to uid,
                "code" to code,
                "title" to cleanTitle,
                "linkedHazardCode" to cleanLinked,
                "ownerName" to cleanOwner,
                "dueDateLabel" to cleanDue,
                "priority" to priority.name,
                "status" to status.name,
                "currentStep" to step.toLong(),
                "rootCauseSummary" to cleanRoot,
                "overdue" to overdue,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            db.collection("users").document(uid).collection("capas").document(docId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun advanceCapaStep(
        userId: String,
        capaId: String,
        currentStep: Int,
    ): Result<Unit> {
        val path = "users/$userId/capas/$capaId"
        return try {
            val uid = requireUserId(userId)
            require(capaId.isNotBlank()) { "CAPA ID must not be blank." }
            val nextStep = (currentStep + 1).coerceAtMost(3)
            val nextStatus = CapaStatus.fromStep(nextStep)
            db.collection("users").document(uid).collection("capas").document(capaId)
                .update(
                    mapOf(
                        "currentStep" to nextStep.toLong(),
                        "status" to nextStatus.name,
                        "overdue" to if (nextStatus == CapaStatus.CLOSED) false else false,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCapa(userId: String, capaId: String): Result<Unit> {
        val path = "users/$userId/capas/$capaId"
        return try {
            val uid = requireUserId(userId)
            require(capaId.isNotBlank()) { "CAPA ID must not be blank." }
            db.collection("users").document(uid).collection("capas").document(capaId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun createAudit(
        userId: String,
        title: String,
        zone: String,
        inspectorName: String,
        checklistLabels: List<String>,
        initialStates: List<CheckItemState>? = null,
        notes: String = "",
        customCode: String? = null,
    ): Result<Unit> {
        val docId = "aud_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$userId/audits/$docId"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim()
            val cleanZone = zone.trim()
            val cleanInspector = inspectorName.trim()
            val labels = checklistLabels.map { it.trim() }.filter { it.isNotEmpty() }.take(20)
            require(cleanTitle.length in 3..140) { "Audit title must be 3–140 characters." }
            require(cleanZone.length in 2..100) { "Zone must be 2–100 characters." }
            require(cleanInspector.length in 2..80) { "Inspector name must be 2–80 characters." }
            require(labels.isNotEmpty()) { "At least one checklist item is required." }

            val states = if (initialStates != null && initialStates.size == labels.size) {
                initialStates.map { it.name }
            } else {
                List(labels.size) { CheckItemState.PENDING.name }
            }
            val passedCount = states.count { it == CheckItemState.PASS.name }
            val status = when {
                states.all { it != CheckItemState.PENDING.name } -> AuditStatus.COMPLETED
                states.any { it != CheckItemState.PENDING.name } -> AuditStatus.IN_PROGRESS
                else -> AuditStatus.PENDING
            }
            val code = (customCode?.trim()?.takeIf { it.length in 3..32 })
                ?: "AUD-2026-${(100..999).random()}"

            val payload = mapOf(
                "userId" to uid,
                "code" to code,
                "title" to cleanTitle,
                "zone" to cleanZone,
                "inspectorName" to cleanInspector,
                "status" to status.name,
                "passedCount" to passedCount.toLong(),
                "totalCount" to labels.size.toLong(),
                "checklistLabels" to labels,
                "checklistStates" to states,
                "notes" to notes.trim().take(500),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )

            db.collection("users").document(uid).collection("audits").document(docId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateAuditCheckItem(
        userId: String,
        auditId: String,
        currentStates: List<String>,
        itemIndex: Int,
        newState: CheckItemState,
        markCompleted: Boolean = false,
    ): Result<Unit> {
        val path = "users/$userId/audits/$auditId"
        return try {
            val uid = requireUserId(userId)
            require(auditId.isNotBlank()) { "Audit ID must not be blank." }
            require(itemIndex in currentStates.indices) { "Checklist index out of bounds." }
            val updatedStates = currentStates.toMutableList().apply {
                this[itemIndex] = newState.name
            }
            val passedCount = updatedStates.count { it == CheckItemState.PASS.name }
            val nextStatus = when {
                markCompleted -> AuditStatus.COMPLETED
                updatedStates.all { it != CheckItemState.PENDING.name } -> AuditStatus.COMPLETED
                else -> AuditStatus.IN_PROGRESS
            }

            db.collection("users").document(uid).collection("audits").document(auditId)
                .update(
                    mapOf(
                        "checklistStates" to updatedStates,
                        "passedCount" to passedCount.toLong(),
                        "status" to nextStatus.name,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun completeAudit(
        userId: String,
        auditId: String,
        notes: String,
    ): Result<Unit> {
        val path = "users/$userId/audits/$auditId"
        return try {
            val uid = requireUserId(userId)
            require(auditId.isNotBlank()) { "Audit ID must not be blank." }
            db.collection("users").document(uid).collection("audits").document(auditId)
                .update(
                    mapOf(
                        "notes" to notes.trim().take(500),
                        "status" to AuditStatus.COMPLETED.name,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAudit(userId: String, auditId: String): Result<Unit> {
        val path = "users/$userId/audits/$auditId"
        return try {
            val uid = requireUserId(userId)
            require(auditId.isNotBlank()) { "Audit ID must not be blank." }
            db.collection("users").document(uid).collection("audits").document(auditId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun seedSampleFacilityData(userId: String, inspectorName: String): Result<Unit> {
        return try {
            val uid = requireUserId(userId)
            val safeInspector = inspectorName.trim().ifEmpty { "Safety Officer" }.take(80)

            val batch = db.batch()
            val userDoc = db.collection("users").document(uid)

            val h1 = userDoc.collection("hazards").document("haz_sample_089")
            batch.set(
                h1,
                mapOf(
                    "userId" to uid,
                    "code" to "NM-2026-089",
                    "title" to "High-pressure steam flange condensation seepage",
                    "description" to "Intermittent steam spray observed on manifold gasket V-402 during shift handover pressure ramp.",
                    "category" to HazardCategory.UNSAFE_CONDITION.name,
                    "severity" to SeverityLevel.CRITICAL.name,
                    "status" to HazardStatus.STOP_WORK.name,
                    "location" to "Sector B • Compressor Bay 4",
                    "likelihood" to 4L,
                    "impact" to 5L,
                    "riskScore" to 20L,
                    "ppeChecklist" to listOf("Hard Hat", "Face Shield", "Thermal Gloves", "Hearing Protection"),
                    "stopWorkIssued" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val h2 = userDoc.collection("hazards").document("haz_sample_104")
            batch.set(
                h2,
                mapOf(
                    "userId" to uid,
                    "code" to "NM-2026-104",
                    "title" to "Unsecured mezzanine pallet edge near hoist bay",
                    "description" to "Forklift operator spotted cantilevered pallet overhang above pedestrian walkway without toe-board barrier.",
                    "category" to HazardCategory.NEAR_MISS.name,
                    "severity" to SeverityLevel.HIGH.name,
                    "status" to HazardStatus.OPEN.name,
                    "location" to "Warehouse North • Aisle 12",
                    "likelihood" to 3L,
                    "impact" to 4L,
                    "riskScore" to 12L,
                    "ppeChecklist" to listOf("Hard Hat", "Hi-Vis Vest", "Steel-Toe Boots"),
                    "stopWorkIssued" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val h3 = userDoc.collection("hazards").document("haz_sample_072")
            batch.set(
                h3,
                mapOf(
                    "userId" to uid,
                    "code" to "ENV-2026-072",
                    "title" to "Secondary containment tray hydraulic drip",
                    "description" to "Minor hydraulic fluid accumulation contained inside bund tray; absorbent pads deployed.",
                    "category" to HazardCategory.ENVIRONMENTAL.name,
                    "severity" to SeverityLevel.MEDIUM.name,
                    "status" to HazardStatus.IN_PROGRESS.name,
                    "location" to "Tank Farm South • Pump Skid 2",
                    "likelihood" to 2L,
                    "impact" to 3L,
                    "riskScore" to 6L,
                    "ppeChecklist" to listOf("Safety Glasses", "Chemical Gloves", "Steel-Toe Boots"),
                    "stopWorkIssued" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val c1 = userDoc.collection("capas").document("capa_sample_041")
            batch.set(
                c1,
                mapOf(
                    "userId" to uid,
                    "code" to "CAPA-2026-041",
                    "title" to "Replace flange gasket & install pressure relief interlock",
                    "linkedHazardCode" to "NM-2026-089",
                    "ownerName" to "M. Vance",
                    "dueDateLabel" to "Nov 18, 2026",
                    "priority" to SeverityLevel.CRITICAL.name,
                    "status" to CapaStatus.VERIFICATION.name,
                    "currentStep" to 2L,
                    "rootCauseSummary" to "Thermal cycling fatigued Grade-B spiral wound gasket beyond rated torque window.",
                    "overdue" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val c2 = userDoc.collection("capas").document("capa_sample_038")
            batch.set(
                c2,
                mapOf(
                    "userId" to uid,
                    "code" to "CAPA-2026-038",
                    "title" to "Install laser-guided dock edge barrier & spotter protocol",
                    "linkedHazardCode" to "NM-2026-104",
                    "ownerName" to "D. Kowalski",
                    "dueDateLabel" to "Nov 29, 2026",
                    "priority" to SeverityLevel.HIGH.name,
                    "status" to CapaStatus.IN_PROGRESS.name,
                    "currentStep" to 1L,
                    "rootCauseSummary" to "Missing visual load-limit stripe and high-traffic shift crossover at Aisle 12.",
                    "overdue" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val a1 = userDoc.collection("audits").document("aud_sample_019")
            batch.set(
                a1,
                mapOf(
                    "userId" to uid,
                    "code" to "AUD-2026-019",
                    "title" to "Confined Space & LOTO Verification",
                    "zone" to "Sector B • Tank Farm North",
                    "inspectorName" to safeInspector,
                    "status" to AuditStatus.IN_PROGRESS.name,
                    "passedCount" to 3L,
                    "totalCount" to 5L,
                    "checklistLabels" to listOf(
                        "Zero-energy isolation tag & personal lock verified",
                        "Atmospheric O2 (19.5%-23.5%) & LEL <10% logged",
                        "Standby rescue attendant stationed at entry hatch",
                        "Intrinsically safe lighting & comms check passed",
                        "Emergency extraction tripod anchored & inspected"
                    ),
                    "checklistStates" to listOf(
                        CheckItemState.PASS.name,
                        CheckItemState.PASS.name,
                        CheckItemState.PASS.name,
                        CheckItemState.PENDING.name,
                        CheckItemState.PENDING.name,
                    ),
                    "notes" to "Isolation tags verified by shift lead.",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val a2 = userDoc.collection("audits").document("aud_sample_014")
            batch.set(
                a2,
                mapOf(
                    "userId" to uid,
                    "code" to "AUD-2026-014",
                    "title" to "Hot Work & Welding Perimeter Audit",
                    "zone" to "Fabrication Bay 1",
                    "inspectorName" to safeInspector,
                    "status" to AuditStatus.COMPLETED.name,
                    "passedCount" to 4L,
                    "totalCount" to 4L,
                    "checklistLabels" to listOf(
                        "Combustibles cleared within 11m radius",
                        "ABC fire extinguisher pressurized & tagged",
                        "Welding screens & fume extractor active",
                        "30-minute post-work fire watch assigned"
                    ),
                    "checklistStates" to listOf(
                        CheckItemState.PASS.name,
                        CheckItemState.PASS.name,
                        CheckItemState.PASS.name,
                        CheckItemState.PASS.name,
                    ),
                    "notes" to "All hot-work controls compliant for Shift B.",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$userId")
            Result.failure(e)
        }
    }
}
