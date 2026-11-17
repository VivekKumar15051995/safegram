package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CapaAction
import com.example.data.CapaStatus
import com.example.data.SeverityLevel
import com.example.ui.UiState
import com.example.ui.components.CapaMilestoneStepper
import com.example.ui.components.EmptyModuleCard
import com.example.ui.components.SafeGramFilterChip
import com.example.ui.components.SafeGramPrimaryButton
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.AlertAmber
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

enum class CapaFilter(val label: String) {
    ALL("All"),
    OVERDUE("Overdue"),
    ACTIVE("Active"),
    CLOSED("Verified / Closed"),
}

@Composable
fun CapaScreen(
    capasState: UiState<List<CapaAction>>,
    isSubmitting: Boolean,
    onCreateCapa: (
        title: String,
        linkedHazardCode: String?,
        ownerName: String,
        dueDateLabel: String,
        priority: SeverityLevel,
        rootCauseSummary: String,
    ) -> Unit,
    onAdvanceStep: (CapaAction) -> Unit,
    onDeleteCapa: (String) -> Unit,
) {
    var activeFilter by remember { mutableStateOf(CapaFilter.ALL) }
    var showNewCapaDialog by remember { mutableStateOf(false) }

    val capas = (capasState as? UiState.Success)?.data.orEmpty()
    val filteredCapas = remember(capas, activeFilter) {
        capas.filter { c ->
            when (activeFilter) {
                CapaFilter.ALL -> true
                CapaFilter.OVERDUE -> c.overdue && !c.isTerminal
                CapaFilter.ACTIVE -> !c.isTerminal
                CapaFilter.CLOSED -> c.isTerminal
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "CAPA REMEDIATION TRACKER",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateSecondary,
                        )
                        Text(
                            text = "${capas.count { !it.isTerminal }} Active • ${capas.count { it.overdue && !it.isTerminal }} Overdue",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    SafeGramPrimaryButton(
                        text = "+ New CAPA",
                        onClick = { showNewCapaDialog = true },
                        testTag = "new_capa_btn",
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CapaFilter.entries.forEach { filter ->
                        SafeGramFilterChip(
                            label = filter.label,
                            selected = activeFilter == filter,
                            onClick = { activeFilter = filter },
                            testTag = "capa_filter_${filter.name}",
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = BorderHairline)

        when (capasState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SafetyEmerald)
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = capasState.message,
                        color = HazardRedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (filteredCapas.isEmpty()) {
                        item {
                            EmptyModuleCard(
                                title = "No CAPA Actions Found",
                                subtitle = "Create a Corrective & Preventive Action to track root-cause remediation milestones.",
                                primaryLabel = "+ Create CAPA Action",
                                onPrimaryClick = { showNewCapaDialog = true },
                            )
                        }
                    } else {
                        items(filteredCapas, key = { it.id }) { capa ->
                            CapaMilestoneCard(
                                capa = capa,
                                isSubmitting = isSubmitting,
                                onAdvanceStep = { onAdvanceStep(capa) },
                                onDelete = { onDeleteCapa(capa.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewCapaDialog) {
        var title by remember { mutableStateOf("") }
        var linkedHazard by remember { mutableStateOf("") }
        var owner by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("Dec 05, 2026") }
        var rootCause by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(SeverityLevel.HIGH) }

        AlertDialog(
            onDismissRequest = { showNewCapaDialog = false },
            title = { Text("Create CAPA Action") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Remediation Action Title *") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = owner,
                        onValueChange = { owner = it },
                        label = { Text("Assigned Owner *") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Target Due Date *") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = linkedHazard,
                        onValueChange = { linkedHazard = it },
                        label = { Text("Linked Hazard Code (Optional)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = rootCause,
                        onValueChange = { rootCause = it },
                        label = { Text("Root Cause Summary") },
                        minLines = 2,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(SeverityLevel.CRITICAL, SeverityLevel.HIGH, SeverityLevel.MEDIUM).forEach { lvl ->
                            SafeGramFilterChip(
                                label = lvl.label,
                                selected = priority == lvl,
                                onClick = { priority = lvl },
                                testTag = "capa_prio_${lvl.name}",
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SafeGramPrimaryButton(
                    text = "Assign CAPA",
                    onClick = {
                        if (title.trim().length >= 3 && owner.trim().length >= 2 && dueDate.trim().length >= 2) {
                            onCreateCapa(
                                title.trim(),
                                linkedHazard.trim().ifBlank { null },
                                owner.trim(),
                                dueDate.trim(),
                                priority,
                                rootCause.trim(),
                            )
                            showNewCapaDialog = false
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showNewCapaDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun CapaMilestoneCard(
    capa: CapaAction,
    isSubmitting: Boolean,
    onAdvanceStep: () -> Unit,
    onDelete: () -> Unit,
) {
    val accentColor = when {
        capa.isTerminal -> SafetyEmerald
        capa.overdue || capa.priorityEnum == SeverityLevel.CRITICAL -> HazardRed
        else -> AlertAmber
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("capa_card_${capa.code}"),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = capa.code,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                        )
                        if (!capa.linkedHazardCode.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SlateSubtle,
                            ) {
                                Text(
                                    text = capa.linkedHazardCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (capa.overdue && !capa.isTerminal) {
                            SeverityBadge(
                                severity = SeverityLevel.CRITICAL,
                                statusOverride = "OVERDUE",
                            )
                        } else {
                            SeverityBadge(
                                severity = capa.priorityEnum,
                                statusOverride = if (capa.isTerminal) "CLOSED CAPA" else null,
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete CAPA",
                                tint = SlateMuted,
                            )
                        }
                    }
                }

                Text(
                    text = capa.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Owner: ${capa.ownerName} • Due: ${capa.dueDateLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSecondary,
                )

                if (capa.rootCauseSummary.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SlateSubtle,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = capa.rootCauseSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }

                CapaMilestoneStepper(capa = capa)

                if (capa.isTerminal) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SafetyEmeraldContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = SafetyEmeraldText,
                            )
                            Text(
                                text = "CAPA Closed & Verified",
                                style = MaterialTheme.typography.labelMedium,
                                color = SafetyEmeraldText,
                            )
                        }
                    }
                } else {
                    val nextStage = CapaStatus.fromStep(capa.currentStep.toInt() + 1)
                    SafeGramPrimaryButton(
                        text = "Advance to ${nextStage.label}",
                        enabled = !isSubmitting,
                        onClick = onAdvanceStep,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "advance_capa_${capa.code}",
                    )
                }
            }
        }
    }
}
