package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AccessRole
import com.example.data.EhsRepository
import com.example.data.OperatorProfile
import com.example.ui.components.SafeGramFilterChip
import com.example.ui.components.SafeGramPrimaryButton
import com.example.ui.screens.AuditsScreen
import com.example.ui.screens.CapaScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.ReportHazardScreen
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SafetyEmerald
import com.example.ui.theme.SafetyEmeraldAccent
import com.example.ui.theme.SafetyEmeraldContainer
import com.example.ui.theme.SafetyEmeraldText
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_INCIDENTS = "incidents"
private const val ROUTE_AUDITS = "audits"
private const val ROUTE_CAPA = "capa"
private const val ROUTE_REPORT = "report"

private enum class TopTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD(ROUTE_DASHBOARD, "Command", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    INCIDENTS(ROUTE_INCIDENTS, "Hazards", Icons.Filled.ReportProblem, Icons.Outlined.ReportProblem),
    AUDITS(ROUTE_AUDITS, "Audits", Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
    CAPA(ROUTE_CAPA, "CAPA", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser),
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun SafeGramApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)

    var pendingRegistration by remember { mutableStateOf<PendingOperatorRegistration?>(null) }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { reg ->
                pendingRegistration = reg
            },
        )
    } else {
        key(user.uid) {
            SafeGramAuthenticatedShell(
                currentUser = user,
                pendingRegistration = pendingRegistration,
                onRegistrationConsumed = { pendingRegistration = null },
            )
        }
    }
}

@Composable
private fun SafeGramAuthenticatedShell(
    currentUser: FirebaseUser,
    pendingRegistration: PendingOperatorRegistration?,
    onRegistrationConsumed: () -> Unit,
) {
    val uid = currentUser.uid
    val displayName = currentUser.displayName ?: currentUser.email ?: "Safety Officer"
    val email = currentUser.email ?: ""

    val ehsViewModel: EhsViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                EhsViewModel(
                    repository = EhsRepository(db),
                    currentUserId = uid,
                    currentUserName = displayName,
                    currentUserEmail = email,
                )
            }
        },
    )

    LaunchedEffect(pendingRegistration) {
        if (pendingRegistration != null) {
            ehsViewModel.saveOperatorProfile(
                fullName = pendingRegistration.fullName,
                badgeId = pendingRegistration.badgeId,
                role = pendingRegistration.role,
                facilityZone = pendingRegistration.facilityZone,
                showToast = true,
            )
            onRegistrationConsumed()
        }
    }

    val operatorProfile by ehsViewModel.operatorProfile.collectAsStateWithLifecycle()
    val hazardsState by ehsViewModel.hazardsState.collectAsStateWithLifecycle()
    val capasState by ehsViewModel.capasState.collectAsStateWithLifecycle()
    val auditsState by ehsViewModel.auditsState.collectAsStateWithLifecycle()
    val isSubmitting by ehsViewModel.isSubmitting.collectAsStateWithLifecycle()
    val feedbackMessage by ehsViewModel.feedbackMessage.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    var selectedHazardId by remember { mutableStateOf<String?>(null) }
    var presetStopWork by remember { mutableStateOf(false) }
    var showAccessProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(feedbackMessage) {
        val msg = feedbackMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            ehsViewModel.clearFeedback()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    fun openReportScreen(stopWork: Boolean) {
        presetStopWork = stopWork
        navController.navigate(ROUTE_REPORT) {
            launchSingleTop = true
        }
    }

    fun switchTab(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isReportScreen = currentRoute == ROUTE_REPORT

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (!isReportScreen) {
                    SafeGramTopBar(
                        currentUser = currentUser,
                        operatorProfile = operatorProfile,
                        onOpenAccessProfile = { showAccessProfileDialog = true },
                        onSignOut = {
                            onRegistrationConsumed()
                            signOut(credentialManager = credentialManager, scope = scope)
                        },
                    )
                }
            },
            bottomBar = {
                if (!useRail && !isReportScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars,
                    ) {
                        TopTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { switchTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SafetyEmerald,
                                    selectedTextColor = SafetyEmeraldText,
                                    indicatorColor = SafetyEmeraldContainer,
                                    unselectedIconColor = SlateMuted,
                                    unselectedTextColor = SlateMuted,
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!isReportScreen && currentRoute != ROUTE_DASHBOARD) {
                    FloatingActionButton(
                        onClick = { openReportScreen(false) },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = SafetyEmerald,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_report_hazard"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAlert,
                            contentDescription = "Report Hazard",
                        )
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail && !isReportScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        TopTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { switchTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SafetyEmerald,
                                    selectedTextColor = SafetyEmeraldText,
                                    indicatorColor = SafetyEmeraldContainer,
                                    unselectedIconColor = SlateMuted,
                                    unselectedTextColor = SlateMuted,
                                ),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BorderHairline),
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD,
                    modifier = Modifier.weight(1f),
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            hazardsState = hazardsState,
                            capasState = capasState,
                            auditsState = auditsState,
                            isSubmitting = isSubmitting,
                            onReportHazardClick = { stopWork -> openReportScreen(stopWork) },
                            onNavigateToIncidents = { switchTab(ROUTE_INCIDENTS) },
                            onNavigateToAudits = { switchTab(ROUTE_AUDITS) },
                            onNavigateToCapa = { switchTab(ROUTE_CAPA) },
                            onHazardClick = { hazard ->
                                selectedHazardId = hazard.id
                                switchTab(ROUTE_INCIDENTS)
                            },
                            onSeedSampleData = { ehsViewModel.seedSampleFacilityData() },
                        )
                    }

                    composable(ROUTE_INCIDENTS) {
                        IncidentsScreen(
                            hazardsState = hazardsState,
                            selectedHazardId = selectedHazardId,
                            onSelectHazard = { selectedHazardId = it },
                            onReportHazardClick = { openReportScreen(false) },
                            onUpdateStatus = { id, status, stopWork ->
                                ehsViewModel.updateHazardStatus(id, status, stopWork)
                            },
                            onCreateLinkedCapa = { hazard, owner, due ->
                                ehsViewModel.createCapa(
                                    title = "Remediate: ${hazard.title}",
                                    linkedHazardCode = hazard.code,
                                    ownerName = owner,
                                    dueDateLabel = due,
                                    priority = hazard.severityEnum,
                                    rootCauseSummary = hazard.description,
                                )
                            },
                            onDeleteHazard = { id -> ehsViewModel.deleteHazard(id) },
                            isSubmitting = isSubmitting,
                        )
                    }

                    composable(ROUTE_AUDITS) {
                        AuditsScreen(
                            auditsState = auditsState,
                            isSubmitting = isSubmitting,
                            onCreateAudit = { title, zone, labels ->
                                ehsViewModel.createAudit(title, zone, labels)
                            },
                            onSetItemState = { audit, index, state ->
                                ehsViewModel.setAuditItemState(audit, index, state)
                            },
                            onCompleteAudit = { audit, notes ->
                                ehsViewModel.completeAudit(audit, notes)
                            },
                        )
                    }

                    composable(ROUTE_CAPA) {
                        CapaScreen(
                            capasState = capasState,
                            isSubmitting = isSubmitting,
                            onCreateCapa = { title, linkedCode, owner, due, priority, rootCause ->
                                ehsViewModel.createCapa(
                                    title = title,
                                    linkedHazardCode = linkedCode,
                                    ownerName = owner,
                                    dueDateLabel = due,
                                    priority = priority,
                                    rootCauseSummary = rootCause,
                                )
                            },
                            onAdvanceStep = { capa -> ehsViewModel.advanceCapaStep(capa) },
                            onDeleteCapa = { id -> ehsViewModel.deleteCapa(id) },
                        )
                    }

                    composable(ROUTE_REPORT) {
                        ReportHazardScreen(
                            initialStopWork = presetStopWork,
                            isSubmitting = isSubmitting,
                            onBack = { navController.popBackStack() },
                            onSubmit = { title, desc, category, location, likelihood, impact, ppe, stopWork, evidenceUri ->
                                ehsViewModel.submitHazard(
                                    title = title,
                                    description = desc,
                                    category = category,
                                    location = location,
                                    likelihood = likelihood,
                                    impact = impact,
                                    ppeChecklist = ppe,
                                    stopWorkIssued = stopWork,
                                    evidenceUri = evidenceUri,
                                    onSuccess = { navController.popBackStack() },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAccessProfileDialog) {
        var name by remember(operatorProfile, displayName) {
            mutableStateOf(operatorProfile?.fullName?.ifBlank { displayName } ?: displayName)
        }
        var badge by remember(operatorProfile) {
            mutableStateOf(operatorProfile?.badgeId?.ifBlank { "EHS-0401" } ?: "EHS-0401")
        }
        var zone by remember(operatorProfile) {
            mutableStateOf(operatorProfile?.facilityZone?.ifBlank { "Plant-04 • Sector B" } ?: "Plant-04 • Sector B")
        }
        var role by remember(operatorProfile) {
            mutableStateOf(operatorProfile?.roleEnum ?: AccessRole.FIELD_OFFICER)
        }

        AlertDialog(
            onDismissRequest = { showAccessProfileDialog = false },
            title = { Text("Operator Access Control") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Signed in as $email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSecondary,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Operator Name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = badge,
                        onValueChange = { badge = it },
                        label = { Text("Badge ID") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = zone,
                        onValueChange = { zone = it },
                        label = { Text("Facility / Sector") },
                        singleLine = true,
                    )
                    Text(
                        text = "ACCESS ROLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateSecondary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AccessRole.entries.forEach { item ->
                            SafeGramFilterChip(
                                label = item.label,
                                selected = role == item,
                                onClick = { role = item },
                                testTag = "dialog_role_${item.name}",
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SafeGramPrimaryButton(
                    text = "Save Profile",
                    onClick = {
                        if (name.trim().length >= 2 && badge.trim().length >= 2 && zone.trim().length >= 2) {
                            ehsViewModel.saveOperatorProfile(name, badge, role, zone)
                            showAccessProfileDialog = false
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showAccessProfileDialog = false }) {
                    Text("Close")
                }
            },
        )
    }
}

@Composable
private fun SafeGramTopBar(
    currentUser: FirebaseUser,
    operatorProfile: OperatorProfile?,
    onOpenAccessProfile: () -> Unit,
    onSignOut: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val subtitle = if (operatorProfile != null) {
        "${operatorProfile.roleEnum.label.uppercase()} • ${operatorProfile.badgeId}"
    } else {
        "ENTERPRISE SAFETY COMMAND"
    }

    Surface(
        color = SlateDark,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SafetyEmerald, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "SafeGram EHS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = SafetyEmeraldAccent,
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("user_menu_btn"),
                ) {
                    val photoUrl = currentUser.photoUrl?.toString()
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Safety officer profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SafetyEmerald, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (currentUser.displayName?.firstOrNull() ?: 'S').uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = operatorProfile?.fullName?.ifBlank { null }
                                        ?: currentUser.displayName
                                        ?: "Safety Officer",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    text = currentUser.email ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SlateMuted,
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onOpenAccessProfile()
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Operator Access Profile") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenAccessProfile()
                        },
                        modifier = Modifier.testTag("access_profile_menu_item"),
                    )
                    DropdownMenuItem(
                        text = { Text("Sign Out") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onSignOut()
                        },
                        modifier = Modifier.testTag("sign_out_menu_item"),
                    )
                }
            }
        }
    }
}
