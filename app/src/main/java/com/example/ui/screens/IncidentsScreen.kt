package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.HazardCategory
import com.example.data.HazardReport
import com.example.data.HazardStatus
import com.example.data.SeverityLevel
import com.example.ui.UiState
import com.example.ui.components.EmptyModuleCard
import com.example.ui.components.HazardIncidentCard
import com.example.ui.components.RiskScoreCapsule
import com.example.ui.components.SafeGramFilterChip
import com.example.ui.components.SafeGramPrimaryButton
import com.example.ui.components.SafeGramSecondaryButton
import com.example.ui.components.SeverityBadge
import com.example.ui.components.formatEhsTimestamp
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BorderInput
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

enum class IncidentFilter(val label: String) {
    ALL("All"),
    STOP_WORK("Stop-Work"),
    HIGH_RISK("Critical / High"),
    NEAR_MISS("Near Miss"),
    UNSAFE_CONDITION("Unsafe Cond."),
    RESOLVED("Resolved"),
}

@Composable
fun IncidentsScreen(
    hazardsState: UiState<List<HazardReport>>,
    selectedHazardId: String?,
    onSelectHazard: (String?) -> Unit,
    onReportHazardClick: () -> Unit,
    onUpdateStatus: (hazardId: String, status: HazardStatus, stopWork: Boolean) -> Unit,
    onCreateLinkedCapa: (hazard: HazardReport, owner: String, due: String) -> Unit,
    onDeleteHazard: (hazardId: String) -> Unit,
    isSubmitting: Boolean,
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(IncidentFilter.ALL) }

    val hazards = (hazardsState as? UiState.Success)?.data.orEmpty()
    val filteredHazards = remember(hazards, searchQuery, activeFilter) {
        hazards.filter { h ->
            val matchesQuery = searchQuery.isBlank() ||
                h.title.contains(searchQuery, ignoreCase = true) ||
                h.code.contains(searchQuery, ignoreCase = true) ||
                h.location.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (activeFilter) {
                IncidentFilter.ALL -> true
                IncidentFilter.STOP_WORK -> h.stopWorkIssued || h.statusEnum == HazardStatus.STOP_WORK
                IncidentFilter.HIGH_RISK -> h.severityEnum == SeverityLevel.CRITICAL || h.severityEnum == SeverityLevel.HIGH
                IncidentFilter.NEAR_MISS -> h.categoryEnum == HazardCategory.NEAR_MISS
                IncidentFilter.UNSAFE_CONDITION -> h.categoryEnum == HazardCategory.UNSAFE_CONDITION
                IncidentFilter.RESOLVED -> h.statusEnum == HazardStatus.RESOLVED
            }
            matchesQuery && matchesFilter
        }
    }

    val selectedHazard = hazards.firstOrNull { it.id == selectedHazardId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val isTwoPane = maxWidth >= 700.dp

        if (isTwoPane) {
            Row(modifier = Modifier.fillMaxSize()) {
                IncidentListPane(
                    hazardsState = hazardsState,
                    filteredHazards = filteredHazards,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    activeFilter = activeFilter,
                    onFilterChange = { activeFilter = it },
                    selectedHazardId = selectedHazard?.id,
                    onSelectHazard = { onSelectHazard(it.id) },
                    onReportHazardClick = onReportHazardClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(BorderHairline),
                )
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                ) {
                    val activeDetail = selectedHazard ?: filteredHazards.firstOrNull()
                    if (activeDetail != null) {
                        IncidentDetailPane(
                            hazard = activeDetail,
                            showBackButton = false,
                            onBack = {},
                            onUpdateStatus = onUpdateStatus,
                            onCreateLinkedCapa = onCreateLinkedCapa,
                            onDeleteHazard = {
                                onDeleteHazard(activeDetail.id)
                                onSelectHazard(null)
                            },
                            isSubmitting = isSubmitting,
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Select a hazard observation to inspect details.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMuted,
                            )
                        }
                    }
                }
            }
        } else {
            if (selectedHazard != null) {
                BackHandler { onSelectHazard(null) }
                IncidentDetailPane(
                    hazard = selectedHazard,
                    showBackButton = true,
                    onBack = { onSelectHazard(null) },
                    onUpdateStatus = onUpdateStatus,
                    onCreateLinkedCapa = onCreateLinkedCapa,
                    onDeleteHazard = {
                        onDeleteHazard(selectedHazard.id)
                        onSelectHazard(null)
                    },
                    isSubmitting = isSubmitting,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )
            } else {
                IncidentListPane(
                    hazardsState = hazardsState,
                    filteredHazards = filteredHazards,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    activeFilter = activeFilter,
                    onFilterChange = { activeFilter = it },
                    selectedHazardId = null,
                    onSelectHazard = { onSelectHazard(it.id) },
                    onReportHazardClick = onReportHazardClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun IncidentListPane(
    hazardsState: UiState<List<HazardReport>>,
    filteredHazards: List<HazardReport>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilter: IncidentFilter,
    onFilterChange: (IncidentFilter) -> Unit,
    selectedHazardId: String?,
    onSelectHazard: (HazardReport) -> Unit,
    onReportHazardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search code (NM-2026), sector, or hazard...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = SlateMuted,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyEmerald,
                    unfocusedBorderColor = BorderInput,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incidents_search_input"),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IncidentFilter.entries.forEach { filter ->
                    SafeGramFilterChip(
                        label = filter.label,
                        selected = activeFilter == filter,
                        onClick = { onFilterChange(filter) },
                        testTag = "filter_chip_${filter.name}",
                    )
                }
            }
        }
        HorizontalDivider(color = BorderHairline)

        when (hazardsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SafetyEmerald)
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = hazardsState.message,
                        color = HazardRedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (filteredHazards.isEmpty()) {
                        item {
                            EmptyModuleCard(
                                title = "No Matching Hazards",
                                subtitle = "Adjust your search filter or log a new field observation.",
                                primaryLabel = "+ Report Hazard",
                                onPrimaryClick = onReportHazardClick,
                            )
                        }
                    } else {
                        items(filteredHazards, key = { it.id }) { hazard ->
                            HazardIncidentCard(
                                hazard = hazard,
                                selected = hazard.id == selectedHazardId,
                                onClick = { onSelectHazard(hazard) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncidentDetailPane(
    hazard: HazardReport,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onUpdateStatus: (hazardId: String, status: HazardStatus, stopWork: Boolean) -> Unit,
    onCreateLinkedCapa: (hazard: HazardReport, owner: String, due: String) -> Unit,
    onDeleteHazard: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
) {
    var showCapaDialog by remember { mutableStateOf(false) }
    var capaOwner by remember { mutableStateOf("Plant Safety Lead") }
    var capaDue by remember { mutableStateOf("Nov 30, 2026") }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("detail_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to incidents",
                            )
                        }
                    }
                    Text(
                        text = hazard.code,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark,
                    )
                }

                IconButton(
                    onClick = onDeleteHazard,
                    enabled = !isSubmitting,
                    modifier = Modifier.testTag("detail_delete_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete hazard report",
                        tint = HazardRed,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeverityBadge(
                    severity = hazard.severityEnum,
                    statusOverride = if (hazard.stopWorkIssued && !hazard.isTerminal) {
                        HazardStatus.STOP_WORK.name
                    } else if (hazard.isTerminal) {
                        HazardStatus.RESOLVED.name
                    } else {
                        null
                    },
                )
                RiskScoreCapsule(riskScore = hazard.riskScore.toInt())
            }

            Text(
                text = hazard.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = SafetyEmerald,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${hazard.location} • ${formatEhsTimestamp(hazard.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSecondary,
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SlateSubtle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "OBSERVATION NARRATIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    Text(
                        text = hazard.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricBox(
                    label = "LIKELIHOOD",
                    value = "${hazard.likelihood} / 5",
                    modifier = Modifier.weight(1f),
                )
                MetricBox(
                    label = "IMPACT",
                    value = "${hazard.impact} / 5",
                    modifier = Modifier.weight(1f),
                )
                MetricBox(
                    label = "STATUS",
                    value = hazard.statusEnum.label,
                    modifier = Modifier.weight(1.2f),
                )
            }

            if (hazard.ppeChecklist.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "REQUIRED PPE CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        hazard.ppeChecklist.forEach { ppe ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SafetyEmeraldContainer,
                            ) {
                                Text(
                                    text = ppe,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SafetyEmeraldText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (!hazard.evidenceUri.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ATTACHED EVIDENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    AsyncImage(
                        model = hazard.evidenceUri,
                        contentDescription = "Hazard photo evidence",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }

            HorizontalDivider(color = BorderHairline)

            if (hazard.isTerminal) {
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
                        Text(
                            text = "Resolved & Locked — Compliance archive record sealed.",
                            style = MaterialTheme.typography.labelMedium,
                            color = SafetyEmeraldText,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (hazard.statusEnum != HazardStatus.IN_PROGRESS) {
                            SafeGramSecondaryButton(
                                text = "In Progress",
                                enabled = !isSubmitting,
                                onClick = {
                                    onUpdateStatus(hazard.id, HazardStatus.IN_PROGRESS, false)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "detail_in_progress_btn",
                            )
                        }
                        SafeGramPrimaryButton(
                            text = "Verify & Resolve",
                            enabled = !isSubmitting,
                            onClick = {
                                onUpdateStatus(hazard.id, HazardStatus.RESOLVED, false)
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "detail_resolve_btn",
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SafeGramSecondaryButton(
                            text = "Create Linked CAPA",
                            enabled = !isSubmitting,
                            onClick = { showCapaDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "detail_create_capa_btn",
                        )
                        if (!hazard.stopWorkIssued) {
                            SafeGramPrimaryButton(
                                text = "Issue Stop-Work",
                                enabled = !isSubmitting,
                                isCritical = true,
                                onClick = {
                                    onUpdateStatus(hazard.id, HazardStatus.STOP_WORK, true)
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "detail_stop_work_btn",
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCapaDialog) {
        AlertDialog(
            onDismissRequest = { showCapaDialog = false },
            title = { Text("Assign Linked CAPA") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Linking remediation tracker to ${hazard.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSecondary,
                    )
                    OutlinedTextField(
                        value = capaOwner,
                        onValueChange = { capaOwner = it },
                        label = { Text("Responsible Owner") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = capaDue,
                        onValueChange = { capaDue = it },
                        label = { Text("Target Due Date") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                SafeGramPrimaryButton(
                    text = "Assign CAPA",
                    onClick = {
                        onCreateLinkedCapa(hazard, capaOwner, capaDue)
                        showCapaDialog = false
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showCapaDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SlateSubtle,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = SlateDark,
            )
        }
    }
}
