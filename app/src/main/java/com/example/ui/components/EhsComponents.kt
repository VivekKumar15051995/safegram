package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CapaAction
import com.example.data.CapaStatus
import com.example.data.HazardReport
import com.example.data.HazardStatus
import com.example.data.SeverityLevel
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberContainer
import com.example.ui.theme.AlertAmberText
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BorderInput
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
import com.example.ui.theme.SlateSubtle
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatEhsTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    val formatter = SimpleDateFormat("MMM dd • HH:mm", Locale.US)
    return formatter.format(timestamp.toDate())
}

@Composable
fun SeverityBadge(
    severity: SeverityLevel,
    statusOverride: String? = null,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, dot) = when {
        statusOverride == HazardStatus.RESOLVED.name -> Triple(
            SafetyEmeraldContainer,
            SafetyEmeraldText,
            SafetyEmeraldAccent,
        )
        severity == SeverityLevel.CRITICAL || severity == SeverityLevel.HIGH || statusOverride == HazardStatus.STOP_WORK.name -> Triple(
            HazardRedContainer,
            HazardRedText,
            HazardRed,
        )
        severity == SeverityLevel.MEDIUM -> Triple(
            AlertAmberContainer,
            AlertAmberText,
            AlertAmber,
        )
        else -> Triple(
            SafetyEmeraldContainer,
            SafetyEmeraldText,
            SafetyEmeraldAccent,
        )
    }

    val labelText = statusOverride?.replace("_", " ") ?: "${severity.label} SEVERITY"

    Surface(
        shape = CircleShape,
        color = bg,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dot, CircleShape),
            )
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun RiskScoreCapsule(
    riskScore: Int,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when {
        riskScore >= 16 -> HazardRedContainer to HazardRedText
        riskScore >= 10 -> AlertAmberContainer to AlertAmberText
        riskScore >= 5 -> AlertAmberContainer to AlertAmberText
        else -> SafetyEmeraldContainer to SafetyEmeraldText
    }
    Surface(
        shape = CircleShape,
        color = bg,
        modifier = modifier,
    ) {
        Text(
            text = "RISK $riskScore",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun SafeGramFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (selected) SafetyEmerald else SlateSubtle,
            modifier = Modifier.height(32.dp),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Color.White else SlateSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun HazardIncidentCard(
    hazard: HazardReport,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = when {
        hazard.statusEnum == HazardStatus.RESOLVED -> SafetyEmerald
        hazard.stopWorkIssued || hazard.severityEnum == SeverityLevel.CRITICAL || hazard.severityEnum == SeverityLevel.HIGH -> HazardRed
        hazard.severityEnum == SeverityLevel.MEDIUM -> AlertAmber
        else -> SafetyEmerald
    }

    val borderStroke = if (selected) {
        BorderStroke(2.dp, SafetyEmerald)
    } else {
        BorderStroke(1.dp, BorderHairline)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = borderStroke,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("hazard_card_${hazard.code}"),
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = hazard.code,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateDark,
                    )
                    Text(
                        text = formatEhsTimestamp(hazard.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateMuted,
                    )
                }

                Text(
                    text = hazard.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${hazard.categoryEnum.label} • ${hazard.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hazard.stopWorkIssued && hazard.statusEnum != HazardStatus.RESOLVED) {
                        SeverityBadge(
                            severity = SeverityLevel.CRITICAL,
                            statusOverride = "STOP_WORK",
                        )
                    } else {
                        SeverityBadge(
                            severity = hazard.severityEnum,
                            statusOverride = if (hazard.statusEnum == HazardStatus.RESOLVED) HazardStatus.RESOLVED.name else null,
                        )
                    }
                    RiskScoreCapsule(riskScore = hazard.riskScore.toInt())
                    Spacer(modifier = Modifier.weight(1f))
                    if (hazard.ppeChecklist.isNotEmpty()) {
                        Text(
                            text = "${hazard.ppeChecklist.size} PPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskMatrixSelector(
    selectedLikelihood: Int,
    selectedImpact: Int,
    onSelect: (likelihood: Int, impact: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val score = selectedLikelihood * selectedImpact
    val severity = SeverityLevel.fromRiskScore(score)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "5×5 RISK MATRIX SELECTOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    Text(
                        text = "Likelihood ($selectedLikelihood) × Impact ($selectedImpact)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeverityBadge(severity = severity)
                    RiskScoreCapsule(riskScore = score)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (likelihood in 5 downTo 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "L$likelihood",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted,
                            modifier = Modifier.width(22.dp),
                        )
                        for (impact in 1..5) {
                            val cellScore = likelihood * impact
                            val isSelected = likelihood == selectedLikelihood && impact == selectedImpact
                            val (cellBg, cellFg) = when {
                                cellScore >= 16 -> (if (isSelected) HazardRed else HazardRedContainer) to (if (isSelected) Color.White else HazardRedText)
                                cellScore >= 10 -> (if (isSelected) Color(0xFFEA580C) else Color(0xFFFFEDD5)) to (if (isSelected) Color.White else Color(0xFF9A3412))
                                cellScore >= 5 -> (if (isSelected) AlertAmber else AlertAmberContainer) to (if (isSelected) Color.White else AlertAmberText)
                                else -> (if (isSelected) SafetyEmerald else SafetyEmeraldContainer) to (if (isSelected) Color.White else SafetyEmeraldText)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellBg)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, SlateDark, RoundedCornerShape(4.dp))
                                        } else {
                                            Modifier.border(1.dp, BorderHairline.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        }
                                    )
                                    .clickable { onSelect(likelihood, impact) }
                                    .testTag("risk_cell_${likelihood}_$impact"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = cellScore.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cellFg,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Spacer(modifier = Modifier.width(22.dp))
                    for (impact in 1..5) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "I$impact",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapaMilestoneStepper(
    capa: CapaAction,
    modifier: Modifier = Modifier,
) {
    val stages = CapaStatus.entries
    val currentStep = capa.currentStep.toInt().coerceIn(0, 3)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        stages.forEachIndexed { index, stage ->
            val isCompleted = capa.isTerminal || index < currentStep
            val isActive = !capa.isTerminal && index == currentStep

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                when {
                                    index == 0 -> Color.Transparent
                                    isCompleted || isActive -> SafetyEmerald
                                    else -> BorderInput
                                }
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> SafetyEmerald
                                    isActive -> AlertAmber
                                    else -> SlateSubtle
                                }
                            )
                            .then(
                                if (!isCompleted && !isActive) {
                                    Modifier.border(1.5.dp, BorderInput, CircleShape)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) Color.White else SlateMuted,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                when {
                                    index == stages.lastIndex -> Color.Transparent
                                    isCompleted -> SafetyEmerald
                                    else -> BorderInput
                                }
                            ),
                    )
                }

                Text(
                    text = stage.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isCompleted -> SafetyEmeraldText
                        isActive -> AlertAmberText
                        else -> SlateMuted
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SafeGramCheckboxRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) SafetyEmerald else MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.5.dp,
                    color = if (checked) SafetyEmerald else BorderInput,
                    shape = RoundedCornerShape(4.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SafeGramPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isCritical: Boolean = false,
    testTag: String? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCritical) HazardRed else SafetyEmerald,
            contentColor = Color.White,
        ),
        modifier = modifier
            .height(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun SafeGramSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, BorderInput),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SlateDark,
        ),
        modifier = modifier
            .height(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun EmptyModuleCard(
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SafetyEmeraldContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = SafetyEmerald,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SafeGramPrimaryButton(
                text = primaryLabel,
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth(),
                testTag = "empty_primary_button",
            )
            if (secondaryLabel != null && onSecondaryClick != null) {
                SafeGramSecondaryButton(
                    text = secondaryLabel,
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "empty_secondary_button",
                )
            }
        }
    }
}

@Composable
fun StopWorkAlertBanner(
    stopWorkCount: Int,
    onInspectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HazardRedContainer,
        border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onInspectClick),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(HazardRed),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = HazardRed,
                    modifier = Modifier.size(24.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$stopWorkCount ACTIVE STOP-WORK ORDER${if (stopWorkCount > 1) "S" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = HazardRedText,
                    )
                    Text(
                        text = "Immediate supervisor isolation & remediation sign-off required.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HazardRedText,
                    )
                }
            }
        }
    }
}
