package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AuditStatus
import com.example.data.CheckItemState
import com.example.data.SafetyAudit
import com.example.data.SeverityLevel
import com.example.ui.UiState
import com.example.ui.components.EmptyModuleCard
import com.example.ui.components.SafeGramFilterChip
import com.example.ui.components.SafeGramPrimaryButton
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.AlertAmberText
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.HazardRed
import com.example.ui.theme.HazardRedContainer
import com.example.ui.theme.HazardRedText
import com.example.ui.theme.SafetyEmerald
import com.example.ui.theme.SafetyEmeraldContainer
import com.example.ui.theme.SafetyEmeraldText
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SlateSubtle

private val AuditTemplates = mapOf(
    "Confined Space & LOTO" to listOf(
        "Zero-energy isolation tag & personal lock verified",
        "Atmospheric O2 (19.5%-23.5%) & LEL <10% logged",
        "Standby rescue attendant stationed at entry hatch",
        "Intrinsically safe lighting & comms check passed",
        "Emergency extraction tripod anchored & inspected",
    ),
    "Hot Work & Welding" to listOf(
        "Combustibles cleared within 11m radius",
        "ABC fire extinguisher pressurized & tagged",
        "Welding screens & fume extractor active",
        "30-minute post-work fire watch assigned",
    ),
    "Chemical Storage & Bunds" to listOf(
        "Secondary containment bund free of fluid",
        "SDS binder & spill kit stocked within 10m",
        "Incompatible oxidizers/flammables segregated",
        "Eyewash & safety shower weekly flow tested",
    ),
)

@Composable
fun AuditsScreen(
    auditsState: UiState<List<SafetyAudit>>,
    isSubmitting: Boolean,
    onCreateAudit: (title: String, zone: String, checklistLabels: List<String>) -> Unit,
    onSetItemState: (audit: SafetyAudit, index: Int, newState: CheckItemState) -> Unit,
    onCompleteAudit: (audit: SafetyAudit, notes: String) -> Unit,
) {
    var selectedAuditId by remember { mutableStateOf<String?>(null) }
    var showNewAuditDialog by remember { mutableStateOf(false) }

    val audits = (auditsState as? UiState.Success)?.data.orEmpty()
    val activeAudit = audits.firstOrNull { it.id == selectedAuditId } ?: audits.firstOrNull()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val isTwoPane = maxWidth >= 700.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Sub-header bar with + New Audit
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "FIELD INSPECTION CLIPBOARDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateSecondary,
                        )
                        Text(
                            text = "${audits.size} Protocol${if (audits.size == 1) "" else "s"} Logged",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    SafeGramPrimaryButton(
                        text = "+ New Audit",
                        onClick = { showNewAuditDialog = true },
                        testTag = "new_audit_btn",
                    )
                }
            }
            HorizontalDivider(color = BorderHairline)

            when (auditsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SafetyEmerald)
                    }
                }

                is UiState.Error -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = auditsState.message,
                            color = HazardRedText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                is UiState.Success -> {
                    if (audits.isEmpty()) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            EmptyModuleCard(
                                title = "No Field Audits Active",
                                subtitle = "Launch a standardized LOTO, Hot Work, or Chemical Containment inspection clipboard.",
                                primaryLabel = "+ Start Safety Audit",
                                onPrimaryClick = { showNewAuditDialog = true },
                            )
                        }
                    } else if (isTwoPane) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(audits, key = { it.id }) { audit ->
                                    AuditSummaryCard(
                                        audit = audit,
                                        selected = audit.id == activeAudit?.id,
                                        onClick = { selectedAuditId = audit.id },
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(BorderHairline),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight()
                                    .padding(16.dp),
                            ) {
                                if (activeAudit != null) {
                                    AuditClipboardDetail(
                                        audit = activeAudit,
                                        isSubmitting = isSubmitting,
                                        onSetItemState = onSetItemState,
                                        onCompleteAudit = onCompleteAudit,
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(audits, key = { it.id }) { audit ->
                                val isExpanded = audit.id == (selectedAuditId ?: audits.firstOrNull()?.id)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AuditSummaryCard(
                                        audit = audit,
                                        selected = isExpanded,
                                        onClick = {
                                            selectedAuditId = if (isExpanded) "" else audit.id
                                        },
                                    )
                                    if (isExpanded) {
                                        AuditClipboardDetail(
                                            audit = audit,
                                            isSubmitting = isSubmitting,
                                            onSetItemState = onSetItemState,
                                            onCompleteAudit = onCompleteAudit,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewAuditDialog) {
        var selectedTemplate by remember { mutableStateOf(AuditTemplates.keys.first()) }
        var zone by remember { mutableStateOf("Sector B • Tank Farm North") }

        AlertDialog(
            onDismissRequest = { showNewAuditDialog = false },
            title = { Text("Start Field Audit") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Select Inspection Protocol",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateSecondary,
                    )
                    AuditTemplates.keys.forEach { tpl ->
                        SafeGramFilterChip(
                            label = tpl,
                            selected = selectedTemplate == tpl,
                            onClick = { selectedTemplate = tpl },
                            testTag = "audit_tpl_$tpl",
                        )
                    }
                    OutlinedTextField(
                        value = zone,
                        onValueChange = { zone = it },
                        label = { Text("Plant Zone / Bay") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                SafeGramPrimaryButton(
                    text = "Launch Clipboard",
                    onClick = {
                        val labels = AuditTemplates[selectedTemplate] ?: emptyList()
                        if (zone.trim().length >= 2 && labels.isNotEmpty()) {
                            onCreateAudit(selectedTemplate, zone.trim(), labels)
                            showNewAuditDialog = false
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showNewAuditDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AuditSummaryCard(
    audit: SafetyAudit,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val total = audit.totalCount.toInt().coerceAtLeast(1)
    val passed = audit.passedCount.toInt().coerceIn(0, total)
    val progress = passed.toFloat() / total.toFloat()

    val severity = when (audit.statusEnum) {
        AuditStatus.COMPLETED -> SeverityLevel.LOW
        AuditStatus.IN_PROGRESS -> SeverityLevel.MEDIUM
        AuditStatus.PENDING -> SeverityLevel.MEDIUM
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) SafetyEmerald else BorderHairline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("audit_card_${audit.code}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = audit.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                )
                SeverityBadge(
                    severity = severity,
                    statusOverride = audit.statusEnum.label,
                )
            }
            Text(
                text = audit.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${audit.zone} • Inspector: ${audit.inspectorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateSecondary,
            )
            LinearProgressIndicator(
                progress = { progress },
                color = SafetyEmerald,
                trackColor = SlateSubtle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
            Text(
                text = "$passed / $total checkpoints passed",
                style = MaterialTheme.typography.labelMedium,
                color = SafetyEmeraldText,
            )
        }
    }
}

@Composable
private fun AuditClipboardDetail(
    audit: SafetyAudit,
    isSubmitting: Boolean,
    onSetItemState: (audit: SafetyAudit, index: Int, newState: CheckItemState) -> Unit,
    onCompleteAudit: (audit: SafetyAudit, notes: String) -> Unit,
) {
    var notes by remember(audit.id, audit.notes) { mutableStateOf(audit.notes) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "INSPECTION CHECKLIST",
                style = MaterialTheme.typography.labelSmall,
                color = SlateSecondary,
            )

            audit.checklistLabels.forEachIndexed { index, label ->
                val stateRaw = audit.checklistStates.getOrNull(index) ?: CheckItemState.PENDING.name
                val state = CheckItemState.fromRaw(stateRaw)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSubtle,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${index + 1}. $label",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            CheckStateButton(
                                label = "PASS",
                                active = state == CheckItemState.PASS,
                                activeBg = SafetyEmerald,
                                enabled = !audit.isTerminal,
                                onClick = { onSetItemState(audit, index, CheckItemState.PASS) },
                                modifier = Modifier.weight(1f),
                                testTag = "audit_item_${index}_pass",
                            )
                            CheckStateButton(
                                label = "FAIL",
                                active = state == CheckItemState.FAIL,
                                activeBg = HazardRed,
                                enabled = !audit.isTerminal,
                                onClick = { onSetItemState(audit, index, CheckItemState.FAIL) },
                                modifier = Modifier.weight(1f),
                                testTag = "audit_item_${index}_fail",
                            )
                            CheckStateButton(
                                label = "PENDING",
                                active = state == CheckItemState.PENDING,
                                activeBg = AlertAmber,
                                enabled = !audit.isTerminal,
                                onClick = { onSetItemState(audit, index, CheckItemState.PENDING) },
                                modifier = Modifier.weight(1f),
                                testTag = "audit_item_${index}_pending",
                            )
                        }
                    }
                }
            }

            if (audit.isTerminal) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SafetyEmeraldContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = SafetyEmeraldText,
                        )
                        Column {
                            Text(
                                text = "Audit Completed & Signed Off",
                                style = MaterialTheme.typography.labelLarge,
                                color = SafetyEmeraldText,
                            )
                            if (audit.notes.isNotBlank()) {
                                Text(
                                    text = audit.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SafetyEmeraldText,
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Inspector Sign-Off Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                SafeGramPrimaryButton(
                    text = "Complete & Sign Off Audit",
                    enabled = !isSubmitting,
                    onClick = { onCompleteAudit(audit, notes) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "complete_audit_btn",
                )
            }
        }
    }
}

@Composable
private fun CheckStateButton(
    label: String,
    active: Boolean,
    activeBg: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (active) activeBg else Color.White,
        border = BorderStroke(1.dp, if (active) activeBg else BorderHairline),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) Color.White else SlateSecondary,
            )
        }
    }
}
