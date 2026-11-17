package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.AccessRole
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class AuthMode {
    LOGIN,
    REGISTER,
}

data class PendingOperatorRegistration(
    val fullName: String,
    val badgeId: String,
    val role: AccessRole,
    val facilityZone: String,
)

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope,
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val cred = credentialManager.getCredential(context, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {},
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val cred = credentialManager.getCredential(activityContext, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {},
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (PendingOperatorRegistration?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var fullName by remember { mutableStateOf("") }
    var badgeId by remember { mutableStateOf("") }
    var facilityZone by remember { mutableStateOf("Plant-04 • Sector B") }
    var selectedRole by remember { mutableStateOf(AccessRole.FIELD_OFFICER) }

    var isAutoChecking by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isAutoChecking = false
                onAuthSuccess(null)
            },
            onUnauthenticated = {
                isAutoChecking = false
            },
            scope = scope,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // High-contrast industrial header module
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SlateDark,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SafetyEmerald, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.HealthAndSafety,
                                        contentDescription = stringResource(R.string.cd_safegram_shield),
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.brand_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = "ACCESS CONTROL PORTAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SafetyEmeraldAccent,
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = SafetyEmerald.copy(alpha = 0.2f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = SafetyEmeraldAccent,
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = "SSO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SafetyEmeraldAccent,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (authMode == AuthMode.LOGIN) {
                                "Operator Sign In"
                            } else {
                                "Register Plant Operator"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (authMode == AuthMode.LOGIN) {
                                "Authenticate with your corporate Google credential to unlock Plant-04 hazard logs, audits, and CAPA controls."
                            } else {
                                "Set your operator badge, facility sector, and access role before linking your Google identity."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                        )
                    }
                }

                // Segmented Login / Register Switcher
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SlateSubtle,
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        AuthModeTab(
                            label = "Sign In",
                            selected = authMode == AuthMode.LOGIN,
                            onClick = {
                                authMode = AuthMode.LOGIN
                                errorMessage = null
                            },
                            testTag = "auth_tab_login",
                            modifier = Modifier.weight(1f),
                        )
                        AuthModeTab(
                            label = "Register",
                            selected = authMode == AuthMode.REGISTER,
                            onClick = {
                                authMode = AuthMode.REGISTER
                                errorMessage = null
                            },
                            testTag = "auth_tab_register",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Animated Mode Card
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "auth_mode_transition",
                ) { mode ->
                    if (mode == AuthMode.LOGIN) {
                        LoginAccessCard()
                    } else {
                        RegistrationFormCard(
                            fullName = fullName,
                            onFullNameChange = {
                                fullName = it
                                errorMessage = null
                            },
                            badgeId = badgeId,
                            onBadgeIdChange = {
                                badgeId = it
                                errorMessage = null
                            },
                            facilityZone = facilityZone,
                            onFacilityZoneChange = {
                                facilityZone = it
                                errorMessage = null
                            },
                            selectedRole = selectedRole,
                            onRoleSelect = { selectedRole = it },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HazardRedContainer,
                        border = BorderStroke(1.dp, HazardRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HazardRedText,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SafetyEmeraldContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = SafetyEmeraldText,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.auth_isolation_notice),
                            style = MaterialTheme.typography.labelMedium,
                            color = SafetyEmeraldText,
                        )
                    }
                }

                Button(
                    onClick = {
                        errorMessage = null
                        val pendingRegistration = if (authMode == AuthMode.REGISTER) {
                            val cleanName = fullName.trim()
                            val cleanBadge = badgeId.trim()
                            val cleanZone = facilityZone.trim()
                            when {
                                cleanName.length < 2 -> {
                                    errorMessage = "Enter your full operator name (at least 2 characters)."
                                    return@Button
                                }
                                cleanBadge.length < 2 -> {
                                    errorMessage = "Enter your employee or safety badge ID (e.g., EHS-0412)."
                                    return@Button
                                }
                                cleanZone.length < 2 -> {
                                    errorMessage = "Enter your assigned plant or sector."
                                    return@Button
                                }
                                else -> PendingOperatorRegistration(
                                    fullName = cleanName,
                                    badgeId = cleanBadge,
                                    role = selectedRole,
                                    facilityZone = cleanZone,
                                )
                            }
                        } else {
                            null
                        }

                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess(pendingRegistration)
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            },
                        )
                    },
                    enabled = !isLoading && !isAutoChecking,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyEmerald,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button"),
                ) {
                    if (isLoading || isAutoChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAutoChecking) {
                                stringResource(R.string.auth_checking_session)
                            } else {
                                stringResource(R.string.auth_signing_in)
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    } else {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) {
                                stringResource(R.string.sign_in_with_google)
                            } else {
                                "Register & Continue with Google"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (authMode == AuthMode.LOGIN) {
                            "New plant operator?"
                        } else {
                            "Already registered?"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSecondary,
                    )
                    TextButton(
                        onClick = {
                            authMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("auth_mode_toggle_link"),
                    ) {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Register Access" else "Sign In",
                            style = MaterialTheme.typography.labelLarge,
                            color = SafetyEmerald,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) SlateDark else SlateMuted,
            )
        }
    }
}

@Composable
private fun LoginAccessCard() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "VERIFIED ACCESS CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                color = SlateSecondary,
            )
            AccessFeatureItem(
                title = "Single Sign-On via Credential Manager",
                subtitle = "Zero-password Google identity check backed by Firebase Auth.",
            )
            AccessFeatureItem(
                title = "Role-Scoped Plant Telemetry",
                subtitle = "Your hazard reports, audits, and CAPA milestones remain strictly isolated to your UID.",
            )
            AccessFeatureItem(
                title = "Instant Session Restoration",
                subtitle = "Automatically resumes authorized operator credentials across field shifts.",
            )
        }
    }
}

@Composable
private fun RegistrationFormCard(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    badgeId: String,
    onBadgeIdChange: (String) -> Unit,
    facilityZone: String,
    onFacilityZoneChange: (String) -> Unit,
    selectedRole: AccessRole,
    onRoleSelect: (AccessRole) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "OPERATOR ACCESS PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = SlateSecondary,
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("Full Name *") },
                placeholder = { Text("e.g., Jordan Vance") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = SlateMuted)
                },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyEmerald,
                    unfocusedBorderColor = BorderInput,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_name_input"),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = badgeId,
                    onValueChange = onBadgeIdChange,
                    label = { Text("Badge ID *") },
                    placeholder = { Text("EHS-0412") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Badge, contentDescription = null, tint = SlateMuted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyEmerald,
                        unfocusedBorderColor = BorderInput,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_badge_input"),
                )

                OutlinedTextField(
                    value = facilityZone,
                    onValueChange = onFacilityZoneChange,
                    label = { Text("Plant / Sector *") },
                    placeholder = { Text("Plant-04") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Business, contentDescription = null, tint = SlateMuted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyEmerald,
                        unfocusedBorderColor = BorderInput,
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("reg_zone_input"),
                )
            }

            Text(
                text = "SELECT ACCESS ROLE",
                style = MaterialTheme.typography.labelSmall,
                color = SlateSecondary,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccessRole.entries.forEach { role ->
                    val isSelected = role == selectedRole
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) SafetyEmeraldContainer else SlateSubtle,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SafetyEmerald else BorderHairline,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onRoleSelect(role) }
                            .testTag("reg_role_${role.name}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = if (isSelected) SafetyEmerald else SlateMuted,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) SafetyEmeraldText else SlateDark,
                                )
                                Text(
                                    text = role.scopeSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) SafetyEmeraldText else SlateSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessFeatureItem(
    title: String,
    subtitle: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = SafetyEmerald,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
