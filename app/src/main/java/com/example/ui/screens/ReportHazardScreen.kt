package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.HazardCategory
import com.example.ui.components.RiskMatrixSelector
import com.example.ui.components.SafeGramCheckboxRow
import com.example.ui.components.SafeGramFilterChip
import com.example.ui.components.SafeGramPrimaryButton
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

private val StandardPpeOptions = listOf(
    "Hard Hat",
    "Safety Glasses",
    "Hi-Vis Vest",
    "Steel-Toe Boots",
    "Hearing Protection",
    "Face Shield",
    "Gas Monitor",
)

@Composable
fun ReportHazardScreen(
    initialStopWork: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        category: HazardCategory,
        location: String,
        likelihood: Int,
        impact: Int,
        ppeChecklist: List<String>,
        stopWorkIssued: Boolean,
        evidenceUri: String?,
    ) -> Unit,
) {
    BackHandler(onBack = onBack)

    var selectedCategory by remember { mutableStateOf(HazardCategory.UNSAFE_CONDITION) }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var likelihood by remember { mutableIntStateOf(if (initialStopWork) 4 else 3) }
    var impact by remember { mutableIntStateOf(if (initialStopWork) 5 else 3) }
    var stopWorkIssued by remember(initialStopWork) { mutableStateOf(initialStopWork) }
    var evidenceUri by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val selectedPpe = remember { mutableStateListOf("Hard Hat", "Safety Glasses") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            evidenceUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("report_back_btn"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Column {
                    Text(
                        text = if (stopWorkIssued) "STOP-WORK AUTHORITY REPORT" else "LOG HAZARD OBSERVATION",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (stopWorkIssued) HazardRedText else SlateDark,
                    )
                    Text(
                        text = "Plant-04 Field Safety Protocol",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateMuted,
                    )
                }
            }
        }
        HorizontalDivider(color = BorderHairline)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Category selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HAZARD CLASSIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HazardCategory.entries.forEach { category ->
                            SafeGramFilterChip(
                                label = category.label,
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                testTag = "cat_chip_${category.name}",
                            )
                        }
                    }
                }

                // Core inputs
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                validationError = null
                            },
                            label = { Text("Actionable Hazard Title *") },
                            placeholder = { Text("e.g., Pressurized valve gasket weeping") },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyEmerald,
                                unfocusedBorderColor = BorderInput,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hazard_title_input"),
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                location = it
                                validationError = null
                            },
                            label = { Text("Sector / Equipment Tag Location *") },
                            placeholder = { Text("e.g., Sector B • Compressor Bay 4") },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyEmerald,
                                unfocusedBorderColor = BorderInput,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hazard_location_input"),
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                validationError = null
                            },
                            label = { Text("Field Observation Narrative *") },
                            placeholder = { Text("Describe immediate condition, exposure, and containment status...") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyEmerald,
                                unfocusedBorderColor = BorderInput,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hazard_description_input"),
                        )
                    }
                }

                // 5x5 Risk Matrix Selector
                RiskMatrixSelector(
                    selectedLikelihood = likelihood,
                    selectedImpact = impact,
                    onSelect = { l, i ->
                        likelihood = l
                        impact = i
                    },
                )

                // Quick-Tap Photo Evidence Row (Zero-permission Android Photo Picker)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "VISUAL HAZARD EVIDENCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateSecondary,
                                )
                                Text(
                                    text = "Attach scene photo via Android Photo Picker",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SlateMuted,
                                )
                            }
                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("attach_photo_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = null,
                                    tint = SafetyEmerald,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = if (evidenceUri == null) "Attach" else "Replace",
                                    color = SafetyEmerald,
                                )
                            }
                        }

                        if (evidenceUri != null) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = evidenceUri,
                                    contentDescription = "Selected hazard photo evidence",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                )
                                IconButton(
                                    onClick = { evidenceUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(SlateDark.copy(alpha = 0.7f), RoundedCornerShape(6.dp)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove attached photo",
                                        tint = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                // PPE Compliance Checklist
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "REQUIRED PPE COMPLIANCE CHECKLIST",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateSecondary,
                        )
                        StandardPpeOptions.forEach { ppe ->
                            val isChecked = ppe in selectedPpe
                            SafeGramCheckboxRow(
                                label = ppe,
                                checked = isChecked,
                                onToggle = {
                                    if (isChecked) selectedPpe.remove(ppe) else selectedPpe.add(ppe)
                                },
                                testTag = "ppe_checkbox_$ppe",
                            )
                        }
                    }
                }

                // Stop-Work Authority Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stopWorkIssued) HazardRedContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (stopWorkIssued) HazardRed else BorderHairline,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (stopWorkIssued) HazardRed else SlateMuted,
                            )
                            Column {
                                Text(
                                    text = "Invoke Stop-Work Authority",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (stopWorkIssued) HazardRedText else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Halts bay operations until supervisor verifies hazard control.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (stopWorkIssued) HazardRedText else SlateMuted,
                                )
                            }
                        }
                        Switch(
                            checked = stopWorkIssued,
                            onCheckedChange = { stopWorkIssued = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HazardRed,
                            ),
                            modifier = Modifier.testTag("stop_work_switch"),
                        )
                    }
                }

                if (validationError != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HazardRedContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = validationError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HazardRedText,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }

        // Bottom thumb-reach submit bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, BorderHairline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                SafeGramPrimaryButton(
                    text = when {
                        isSubmitting -> "Submitting to Registry..."
                        stopWorkIssued -> "Issue Stop-Work Order"
                        else -> "Submit Hazard Report"
                    },
                    enabled = !isSubmitting,
                    isCritical = stopWorkIssued,
                    onClick = {
                        val t = title.trim()
                        val l = location.trim()
                        val d = description.trim()
                        when {
                            t.length < 3 -> validationError = "Enter a hazard title (at least 3 characters)."
                            l.length < 2 -> validationError = "Enter the sector or equipment tag location."
                            d.isEmpty() -> validationError = "Enter field observation details."
                            else -> onSubmit(
                                t,
                                d,
                                selectedCategory,
                                l,
                                likelihood,
                                impact,
                                selectedPpe.toList(),
                                stopWorkIssued,
                                evidenceUri,
                            )
                        }
                    },
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(),
                    testTag = "submit_hazard_btn",
                )
            }
        }
    }
}
