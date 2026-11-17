package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AuditStatus
import com.example.data.CapaAction
import com.example.data.CapaStatus
import com.example.data.HazardReport
import com.example.data.HazardStatus
import com.example.data.SafetyAudit
import com.example.ui.UiState
import com.example.ui.components.EmptyModuleCard
import com.example.ui.components.HazardIncidentCard
import com.example.ui.components.SafeGramPrimaryButton
import com.example.ui.components.SafeGramSecondaryButton
import com.example.ui.components.StopWorkAlertBanner
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.HazardRed
import com.example.ui.theme.HazardRedContainer
import com.example.ui.theme.HazardRedText
import com.example.ui.theme.SafetyEmerald
import com.example.ui.theme.SafetyEmeraldAccent
import com.example.ui.theme.SafetyEmeraldContainer
import com.example.ui.theme.SafetyEmeraldText
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSecondary

@Composable
fun DashboardScreen(
    hazardsState: UiState<List<HazardReport>>,
    capasState: UiState<List<CapaAction>>,
    auditsState: UiState<List<SafetyAudit>>,
    isSubmitting: Boolean,
    onReportHazardClick: (stopWorkPreset: Boolean) -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToAudits: () -> Unit,
    onNavigateToCapa: () -> Unit,
    onHazardClick: (HazardReport) -> Unit,
    onSeedSampleData: () -> Unit,
) {
    val hazards = (hazardsState as? UiState.Success)?.data.orEmpty()
    val capas = (capasState as? UiState.Success)?.data.orEmpty()
    val audits = (auditsState as? UiState.Success)?.data.orEmpty()

    val openHazardsCount = hazards.count { it.statusEnum != HazardStatus.RESOLVED }
    val stopWorkCount = hazards.count { it.stopWorkIssued && it.statusEnum != HazardStatus.RESOLVED }
    val openCapasCount = capas.count { it.statusEnum != CapaStatus.CLOSED }
    val overdueCapasCount = capas.count { it.overdue && it.statusEnum != CapaStatus.CLOSED }

    val totalAuditChecks = audits.sumOf { it.totalCount.toInt() }
    val passedAuditChecks = audits.sumOf { it.passedCount.toInt() }
    val complianceRate = if (totalAuditChecks > 0) {
        (passedAuditChecks * 100) / totalAuditChecks
    } else {
        100
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // High-contrast industrial status hero card
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SlateDark,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SafetyEmeraldAccent, CircleShape),
                            )
                            Text(
                                text = "PLANT-04 • LIVE TELEMETRY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1),
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = SafetyEmerald.copy(alpha = 0.25f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = SafetyEmeraldAccent,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "$complianceRate% AUDIT PASS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafetyEmeraldAccent,
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HeroKpiTile(
                            value = openHazardsCount.toString(),
                            label = "OPEN HAZARDS",
                            accent = if (openHazardsCount > 0) AlertAmber else SafetyEmeraldAccent,
                            modifier = Modifier.weight(1f),
                        )
                        HeroKpiTile(
                            value = stopWorkCount.toString(),
                            label = "STOP-WORK",
                            accent = if (stopWorkCount > 0) HazardRed else SafetyEmeraldAccent,
                            modifier = Modifier.weight(1f),
                        )
                        HeroKpiTile(
                            value = openCapasCount.toString(),
                            label = "ACTIVE CAPA",
                            accent = if (overdueCapasCount > 0) HazardRed else Color.White,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Urgent stop-work callout
        if (stopWorkCount > 0) {
            item {
                StopWorkAlertBanner(
                    stopWorkCount = stopWorkCount,
                    onInspectClick = onNavigateToIncidents,
                )
            }
        }

        // Rapid thumb-zone action strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "FIELD DISPATCH ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SafeGramPrimaryButton(
                        text = "+ Report Hazard",
                        onClick = { onReportHazardClick(false) },
                        modifier = Modifier.weight(1.2f),
                        testTag = "dash_report_hazard_btn",
                    )
                    SafeGramPrimaryButton(
                        text = "Stop-Work",
                        onClick = { onReportHazardClick(true) },
                        isCritical = true,
                        modifier = Modifier.weight(0.9f),
                        testTag = "dash_stop_work_btn",
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SafeGramSecondaryButton(
                        text = "Audits (${audits.count { it.statusEnum != AuditStatus.COMPLETED }})",
                        onClick = onNavigateToAudits,
                        modifier = Modifier.weight(1f),
                        testTag = "dash_audits_btn",
                    )
                    SafeGramSecondaryButton(
                        text = "CAPA ($openCapasCount)",
                        onClick = onNavigateToCapa,
                        modifier = Modifier.weight(1f),
                        testTag = "dash_capa_btn",
                    )
                }
            }
        }

        // Loading / Error / Empty / Recent Feed
        when (hazardsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SafetyEmerald)
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HazardRedContainer,
                        border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = hazardsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HazardRedText,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            is UiState.Success -> {
                if (hazards.isEmpty() && capas.isEmpty() && audits.isEmpty()) {
                    item {
                        EmptyModuleCard(
                            title = "Plant Registry Ready",
                            subtitle = "Log your first field hazard observation or load sample Plant-04 EHS records (near misses, LOTO audit, and CAPA trackers) to explore all workflows.",
                            primaryLabel = "+ Report First Hazard",
                            onPrimaryClick = { onReportHazardClick(false) },
                            secondaryLabel = if (isSubmitting) "Loading Sample Records..." else "Load Sample Plant-04 Data",
                            onSecondaryClick = { if (!isSubmitting) onSeedSampleData() },
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "RECENT HAZARD OBSERVATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateSecondary,
                            )
                            Text(
                                text = "${hazards.size} TOTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafetyEmeraldText,
                            )
                        }
                    }

                    if (hazards.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, BorderHairline),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = SafetyEmerald,
                                    )
                                    Text(
                                        text = "No open hazards logged for this shift.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    } else {
                        items(hazards.take(5), key = { it.id }) { hazard ->
                            HazardIncidentCard(
                                hazard = hazard,
                                onClick = { onHazardClick(hazard) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroKpiTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E293B),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                maxLines = 1,
            )
        }
    }
}
