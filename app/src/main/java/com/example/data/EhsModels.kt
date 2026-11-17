package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class HazardCategory(val label: String, val codePrefix: String) {
    NEAR_MISS("Near Miss", "NM"),
    UNSAFE_CONDITION("Unsafe Condition", "UC"),
    UNSAFE_ACT("Unsafe Act", "UA"),
    ENVIRONMENTAL("Environmental", "ENV");

    companion object {
        fun fromRaw(raw: String): HazardCategory =
            entries.firstOrNull { it.name == raw } ?: UNSAFE_CONDITION
    }
}

enum class SeverityLevel(val label: String) {
    CRITICAL("CRITICAL"),
    HIGH("HIGH"),
    MEDIUM("MEDIUM"),
    LOW("LOW");

    companion object {
        fun fromRaw(raw: String): SeverityLevel =
            entries.firstOrNull { it.name == raw } ?: MEDIUM

        fun fromRiskScore(score: Int): SeverityLevel = when {
            score >= 16 -> CRITICAL
            score >= 10 -> HIGH
            score >= 5 -> MEDIUM
            else -> LOW
        }
    }
}

enum class HazardStatus(val label: String) {
    OPEN("OPEN"),
    IN_PROGRESS("IN PROGRESS"),
    RESOLVED("RESOLVED"),
    STOP_WORK("STOP WORK");

    companion object {
        fun fromRaw(raw: String): HazardStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN
    }
}

enum class CapaStatus(val label: String, val stepIndex: Int) {
    OPEN("Root Cause", 0),
    IN_PROGRESS("Action Plan", 1),
    VERIFICATION("Implemented", 2),
    CLOSED("Verified", 3);

    companion object {
        fun fromRaw(raw: String): CapaStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN

        fun fromStep(step: Int): CapaStatus = when {
            step <= 0 -> OPEN
            step == 1 -> IN_PROGRESS
            step == 2 -> VERIFICATION
            else -> CLOSED
        }
    }
}

enum class AuditStatus(val label: String) {
    PENDING("PENDING"),
    IN_PROGRESS("IN PROGRESS"),
    COMPLETED("COMPLETED");

    companion object {
        fun fromRaw(raw: String): AuditStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class CheckItemState {
    PASS,
    FAIL,
    PENDING;

    companion object {
        fun fromRaw(raw: String): CheckItemState =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

@Keep
@IgnoreExtraProperties
data class HazardReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = HazardCategory.UNSAFE_CONDITION.name,
    val severity: String = SeverityLevel.MEDIUM.name,
    val status: String = HazardStatus.OPEN.name,
    val location: String = "",
    val likelihood: Long = 3L,
    val impact: Long = 3L,
    val riskScore: Long = 9L,
    val ppeChecklist: List<String> = emptyList(),
    val stopWorkIssued: Boolean = false,
    val evidenceUri: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val categoryEnum: HazardCategory get() = HazardCategory.fromRaw(category)
    val severityEnum: SeverityLevel get() = SeverityLevel.fromRaw(severity)
    val statusEnum: HazardStatus get() = HazardStatus.fromRaw(status)
    val isTerminal: Boolean get() = statusEnum == HazardStatus.RESOLVED
}

@Keep
@IgnoreExtraProperties
data class CapaAction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val linkedHazardCode: String? = null,
    val ownerName: String = "",
    val dueDateLabel: String = "",
    val priority: String = SeverityLevel.HIGH.name,
    val status: String = CapaStatus.OPEN.name,
    val currentStep: Long = 0L,
    val rootCauseSummary: String = "",
    val overdue: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val priorityEnum: SeverityLevel get() = SeverityLevel.fromRaw(priority)
    val statusEnum: CapaStatus get() = CapaStatus.fromRaw(status)
    val isTerminal: Boolean get() = statusEnum == CapaStatus.CLOSED
}

@Keep
@IgnoreExtraProperties
data class SafetyAudit(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val zone: String = "",
    val inspectorName: String = "",
    val status: String = AuditStatus.PENDING.name,
    val passedCount: Long = 0L,
    val totalCount: Long = 0L,
    val checklistLabels: List<String> = emptyList(),
    val checklistStates: List<String> = emptyList(),
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val statusEnum: AuditStatus get() = AuditStatus.fromRaw(status)
    val isTerminal: Boolean get() = statusEnum == AuditStatus.COMPLETED
}

enum class AccessRole(val label: String, val scopeSummary: String) {
    FIELD_OFFICER(
        label = "Field Officer",
        scopeSummary = "Log hazards, run 5×5 risk matrix, and execute field audits.",
    ),
    EHS_SUPERVISOR(
        label = "EHS Supervisor",
        scopeSummary = "Verify hazard controls, assign CAPAs, and issue Stop-Work orders.",
    ),
    PLANT_MANAGER(
        label = "Plant Manager",
        scopeSummary = "Full site command, CAPA closure sign-off, and audit compliance.",
    );

    companion object {
        fun fromRaw(raw: String): AccessRole =
            entries.firstOrNull { it.name == raw } ?: FIELD_OFFICER
    }
}

@Keep
@IgnoreExtraProperties
data class OperatorProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val badgeId: String = "",
    val role: String = AccessRole.FIELD_OFFICER.name,
    val facilityZone: String = "Plant-04",
    val email: String = "",
    val accessActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val roleEnum: AccessRole get() = AccessRole.fromRaw(role)
}
