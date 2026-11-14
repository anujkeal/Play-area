package com.keal.playarea

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private val HamleysRed = Color(0xFFC8102E)
private val HamleysGold = Color(0xFFD6A84F)
private val Cream = Color(0xFFFFF7EC)
private val Ink = Color(0xFF30251F)
private val Muted = Color(0xFF887A70)
private val EarlyAmber = Color(0xFFB7791F)

private data class ShirtColor(val name: String, val hex: String) {
    val color: Color get() = Color(AndroidColor.parseColor(hex))
}

private val shirtColors = listOf(
    ShirtColor("Red", "#E53935"), ShirtColor("Maroon", "#800000"),
    ShirtColor("Orange", "#F57C00"), ShirtColor("Peach", "#FFAB91"),
    ShirtColor("Yellow", "#FFEB3B"), ShirtColor("Mustard yellow", "#D6A21E"),
    ShirtColor("Lime", "#C0CA33"), ShirtColor("Green", "#2E7D32"),
    ShirtColor("Olive", "#737B25"), ShirtColor("Mint", "#80CBC4"),
    ShirtColor("Teal", "#00897B"), ShirtColor("Sky blue", "#4FC3F7"),
    ShirtColor("Blue", "#1E88E5"), ShirtColor("Navy", "#172A53"),
    ShirtColor("Purple", "#7E57C2"), ShirtColor("Lavender", "#B39DDB"),
    ShirtColor("Pink", "#EC407A"), ShirtColor("Hot pink", "#D81B60"),
    ShirtColor("White", "#FFFFFF"), ShirtColor("Black", "#242424"),
    ShirtColor("Grey", "#9E9E9E"), ShirtColor("Brown", "#795548"),
    ShirtColor("Beige", "#D7C3A5"), ShirtColor("Turquoise", "#26A69A")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SessionAlarmReceiver.ensureChannel(this)
        setContent { PlayAreaTheme { PlayAreaScreen() } }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}

@Composable
private fun PlayAreaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = HamleysRed,
            secondary = HamleysGold,
            background = Cream,
            surface = Color.White,
            onPrimary = Color.White,
            onBackground = Ink,
            onSurface = Ink
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayAreaScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var sessions by remember { mutableStateOf<List<PlaySession>>(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf("Active") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPrevious by remember { mutableStateOf<PlaySession?>(null) }
    var deleteTarget by remember { mutableStateOf<PlaySession?>(null) }

    suspend fun refresh() { sessions = SessionStore.all(context) }

    LaunchedEffect(Unit) {
        SessionStore.initialize(context)
        refresh()
        sessions.filter { it.status == SessionStatus.ACTIVE }.forEach { SessionAlarmReceiver.schedule(context, it) }
    }

    val active = remember(sessions) {
        sessions.filter { it.status == SessionStatus.ACTIVE }.sortedBy { it.endsAt }
    }
    val previous = remember(sessions) {
        sessions.filter { it.status == SessionStatus.ENDED }.sortedByDescending { it.finishedAt ?: 0L }
    }
    val visibleActive = remember(active, query) {
        active.filter { it.name.contains(query, true) || it.colorName.contains(query, true) }
    }
    val visiblePrevious = remember(previous, query) {
        previous.filter { it.name.contains(query, true) || it.colorName.contains(query, true) }
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
    }

    fun endSession(session: PlaySession) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        scope.launch {
            val now = System.currentTimeMillis()
            val elapsed = (now - session.lastStartedAt).coerceAtLeast(0L)
            val ended = session.copy(
                status = SessionStatus.ENDED,
                playedMillis = (session.playedMillis + elapsed).coerceAtMost(SESSION_DURATION_MILLIS),
                finishedAt = now
            )
            SessionStore.update(context, ended)
            SessionAlarmReceiver.cancel(context, session.id)
            refresh()
            if (showUndoSnackbar(snackbarHostState, "Session ended") == SnackbarResult.ActionPerformed) {
                SessionStore.update(context, session)
                SessionAlarmReceiver.schedule(context, session)
                refresh()
            }
        }
    }

    fun deleteSession(session: PlaySession) {
        scope.launch {
            SessionStore.update(context, session.copy(status = SessionStatus.DELETED_PENDING))
            SessionAlarmReceiver.cancel(context, session.id)
            refresh()
            if (showUndoSnackbar(snackbarHostState, "Session deleted") == SnackbarResult.ActionPerformed) {
                SessionStore.update(context, session)
                refresh()
            } else {
                SessionStore.hardDelete(context, session.id)
                refresh()
            }
        }
    }

    fun resumeSession(session: PlaySession) {
        scope.launch {
            if (SessionStore.activeByName(context, session.name) != null) {
                selectedPrevious = null
                showMessage("${session.name} is already playing.")
                return@launch
            }
            val remaining = (SESSION_DURATION_MILLIS - session.playedMillis).coerceAtLeast(0L)
            if (remaining == 0L) {
                selectedPrevious = null
                showMessage("This session has no time remaining.")
                return@launch
            }
            val now = System.currentTimeMillis()
            val resumed = session.copy(
                status = SessionStatus.ACTIVE,
                lastStartedAt = now,
                endsAt = now + remaining,
                finishedAt = null,
                resumeCount = session.resumeCount + 1
            )
            SessionStore.update(context, resumed)
            SessionAlarmReceiver.schedule(context, resumed)
            selectedPrevious = null
            selectedTab = "Active"
            refresh()
        }
    }

    fun restartSession(session: PlaySession) {
        scope.launch {
            if (SessionStore.activeByName(context, session.name) != null) {
                selectedPrevious = null
                showMessage("${session.name} is already playing.")
                return@launch
            }
            val now = System.currentTimeMillis()
            val restarted = PlaySession(
                id = SessionStore.newId(),
                name = session.name,
                colorName = session.colorName,
                colorHex = session.colorHex,
                pattern = session.pattern,
                startedAt = now,
                lastStartedAt = now,
                endsAt = now + SESSION_DURATION_MILLIS,
                status = SessionStatus.ACTIVE,
                playedMillis = 0L,
                resumeCount = 0
            )
            SessionStore.insert(context, restarted)
            SessionAlarmReceiver.schedule(context, restarted)
            selectedPrevious = null
            selectedTab = "Active"
            refresh()
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { focusManager.clearFocus(force = true); showAddSheet = true },
                containerColor = HamleysRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add child", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Play area", color = HamleysRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(
                        "Little moments,\nbig adventures.",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 29.sp,
                        lineHeight = 33.sp,
                        color = Ink
                    )
                }
                Surface(color = Color.White, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0xFFEDE1D3))) {
                    Column(Modifier.padding(horizontal = 17.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${active.size}", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = HamleysRed)
                        Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search name or t-shirt colour") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search sessions", tint = Muted) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0E6D9)).padding(4.dp)) {
                listOf("Active", "Previous").forEach { tab ->
                    val chosen = selectedTab == tab
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(11.dp))
                            .background(if (chosen) Color.White else Color.Transparent)
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (tab == "Active") "Active (${active.size})" else "Previous (${previous.size})",
                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Medium,
                            color = if (chosen) HamleysRed else Muted
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            AnimatedVisibility(
                visible = selectedTab == "Active",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (visibleActive.isEmpty()) {
                    EmptyState(
                        title = if (query.isBlank()) "Ready for some fun?" else "No matching sessions",
                        subtitle = if (query.isBlank()) "Tap + when a child is ready to play." else "Try another name or colour."
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(visibleActive, key = { it.id }) { session ->
                            ActiveSessionCard(session, Modifier.animateItem()) { endSession(session) }
                        }
                        item(key = "active-bottom-space") { Spacer(Modifier.height(84.dp)) }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedTab == "Previous",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (visiblePrevious.isEmpty()) {
                    EmptyState("No previous sessions yet", "Completed play sessions will appear here.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(visiblePrevious, key = { it.id }) { session ->
                            PreviousSessionCard(session, Modifier.animateItem()) { selectedPrevious = session }
                        }
                        item(key = "previous-bottom-space") { Spacer(Modifier.height(84.dp)) }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddChildSheet(
            onDismiss = { showAddSheet = false },
            onStart = { name, color, pattern ->
                scope.launch {
                    val now = System.currentTimeMillis()
                    val session = PlaySession(
                        id = SessionStore.newId(),
                        name = name.trim(),
                        colorName = color.name,
                        colorHex = color.hex,
                        pattern = pattern,
                        startedAt = now,
                        lastStartedAt = now,
                        endsAt = now + SESSION_DURATION_MILLIS
                    )
                    SessionStore.insert(context, session)
                    SessionAlarmReceiver.schedule(context, session)
                    refresh()
                    showAddSheet = false
                }
            }
        )
    }

    selectedPrevious?.let { session ->
        PreviousActionsSheet(
            session = session,
            onDismiss = { selectedPrevious = null },
            onResume = { resumeSession(session) },
            onRestart = { restartSession(session) },
            onDelete = { selectedPrevious = null; deleteTarget = session }
        )
    }

    deleteTarget?.let { session ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this session?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text("${session.name}'s session will be removed from your history.") },
            confirmButton = {
                Button(
                    onClick = { deleteTarget = null; deleteSession(session) },
                    colors = ButtonDefaults.buttonColors(containerColor = HamleysRed),
                    modifier = Modifier.height(48.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }, modifier = Modifier.height(48.dp)) {
                    Text("Cancel", color = Ink)
                }
            },
            containerColor = Cream
        )
    }
}

private suspend fun showUndoSnackbar(host: SnackbarHostState, message: String): SnackbarResult = coroutineScope {
    val result = async {
        host.showSnackbar(
            message = message,
            actionLabel = "Undo",
            withDismissAction = false,
            duration = SnackbarDuration.Indefinite
        )
    }
    while (host.currentSnackbarData?.visuals?.message != message) delay(20)
    val deadline = System.currentTimeMillis() + 6_000L
    while (System.currentTimeMillis() < deadline) {
        if (result.isCompleted && result.await() == SnackbarResult.ActionPerformed) {
            return@coroutineScope SnackbarResult.ActionPerformed
        }
        delay(20)
    }
    if (!result.isCompleted && host.currentSnackbarData?.visuals?.message == message) {
        host.currentSnackbarData?.dismiss()
    }
    result.await()
}

@Composable
private fun ActiveSessionCard(session: PlaySession, modifier: Modifier = Modifier, onEnd: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE1D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(parseColor(session.colorHex)), contentAlignment = Alignment.Center) {
                    Text(
                        session.name.firstOrNull()?.uppercase() ?: "?",
                        color = if (session.colorHex.equals("#FFFFFF", true) || session.colorHex.equals("#FFEB3B", true)) Ink else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(parseColor(session.colorHex)))
                        Spacer(Modifier.width(5.dp))
                        Text(session.colorName, color = Muted, fontSize = 12.sp)
                        Text("  •  ${session.pattern}", color = Muted, fontSize = 12.sp)
                    }
                }
                SessionTimer(session.endsAt)
            }
            Spacer(Modifier.height(13.dp))
            Divider(color = Color(0xFFF0E8DF))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Started ${formatClock(session.startedAt)}", color = Muted, fontSize = 12.sp)
                    Text("Ends ${formatClock(session.endsAt)}", color = Muted, fontSize = 12.sp)
                }
                Button(
                    onClick = onEnd,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HamleysRed)
                ) { Text("End session", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun SessionTimer(endTimeMillis: Long) {
    val now by produceState(initialValue = System.currentTimeMillis(), key1 = endTimeMillis) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val remaining = endTimeMillis - now
    val overtime = remaining <= 0L
    val timerColor = when {
        overtime || remaining < 120_000L -> Color(0xFFC62828)
        remaining < 600_000L -> Color(0xFFEF7D22)
        else -> Color(0xFF2E7D50)
    }
    Column(horizontalAlignment = Alignment.End) {
        Text(if (overtime) "Overtime" else "Time left", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = timerColor)
        Text(
            if (overtime) "+${formatDuration(-remaining)}" else formatDuration(remaining),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
    }
}

@Composable
private fun PreviousSessionCard(session: PlaySession, modifier: Modifier = Modifier, onMore: () -> Unit) {
    val early = session.playedMillis < 60_000L
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE1D3))
    ) {
        Row(Modifier.padding(start = 15.dp, end = 8.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(parseColor(session.colorHex)))
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(session.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${session.colorName}  •  ${formatDate(session.startedAt)}", color = Muted, fontSize = 12.sp)
                Text("${formatClock(session.startedAt)} – ${formatClock(session.finishedAt ?: session.endsAt)}", color = Muted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${session.playedMillis / 60_000L} min", color = HamleysRed, fontWeight = FontWeight.Bold)
                Text(
                    if (early) "Ended early" else "Completed",
                    fontSize = 10.sp,
                    color = if (early) EarlyAmber else Muted,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onMore, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "More actions for ${session.name}'s session", tint = Muted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviousActionsSheet(
    session: PlaySession,
    onDismiss: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val remaining = (SESSION_DURATION_MILLIS - session.playedMillis).coerceAtLeast(0L)
    val remainingMinutes = ceil(remaining / 60_000.0).toInt()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cream,
        modifier = Modifier.fillMaxWidth().height(326.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp).size(width = 42.dp, height = 4.dp),
                color = Color(0xFFD7C8B8),
                shape = CircleShape
            ) {}
        }
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(64.dp)) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(parseColor(session.colorHex)))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.name, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                    Text(
                        "${session.colorName}  •  ${formatClock(session.startedAt)} – ${formatClock(session.finishedAt ?: session.endsAt)}",
                        fontSize = 12.sp,
                        color = Muted
                    )
                }
            }
            Divider(color = Color(0xFFE8DCCF))
            if (remaining > 0L) {
                ActionRow(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HamleysRed) },
                    title = "Resume session",
                    subtitle = "$remainingMinutes min left",
                    onClick = onResume
                )
                Divider(color = Color(0xFFE8DCCF))
            }
            ActionRow(
                icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Ink) },
                title = "Restart 30 min",
                subtitle = "Start a fresh 30-minute session",
                onClick = onRestart
            )
            Divider(color = Color(0xFFE8DCCF))
            ActionRow(
                icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = HamleysRed) },
                title = "Delete session",
                subtitle = "Remove this session from history",
                titleColor = HamleysRed,
                onClick = onDelete
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: Color = Ink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(title, color = titleColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChildSheet(onDismiss: () -> Unit, onStart: (String, ShirtColor, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedColorName by rememberSaveable { mutableStateOf("") }
    var selectedColorHex by rememberSaveable { mutableStateOf("") }
    var pattern by rememberSaveable { mutableStateOf("Plain") }
    var showCustom by rememberSaveable { mutableStateOf(false) }
    val selectedColor = if (selectedColorHex.isBlank()) null else ShirtColor(selectedColorName, selectedColorHex)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val sheetHeight = remember(configuration.screenHeightDp) { configuration.screenHeightDp.dp * 0.92f }
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().height(sheetHeight),
        sheetState = sheetState,
        containerColor = Cream,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp).size(width = 42.dp, height = 4.dp),
                color = Color(0xFFD7C8B8),
                shape = CircleShape
            ) {}
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 20.dp)) {
                Text("New play session", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("A little play goes a long way.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Text("Child's name", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter child's name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { dismissKeyboard() }),
                    shape = RoundedCornerShape(13.dp)
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("T-shirt colour", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                    Spacer(Modifier.weight(1f))
                    Text("Required", color = HamleysRed, fontSize = 11.sp)
                }
                Spacer(Modifier.height(9.dp))
                shirtColors.chunked(3).forEach { rowColors ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowColors.forEach { swatch ->
                            val chosen = selectedColorHex.equals(swatch.hex, ignoreCase = true)
                            Surface(
                                modifier = Modifier.weight(1f).height(48.dp).clickable {
                                    dismissKeyboard()
                                    selectedColorName = swatch.name
                                    selectedColorHex = swatch.hex
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, if (chosen) HamleysRed else Color.Transparent)
                            ) {
                                Row(Modifier.padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(17.dp).clip(CircleShape).background(swatch.color))
                                    Spacer(Modifier.width(5.dp))
                                    Text(swatch.name, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f), color = Ink)
                                    if (chosen) Icon(Icons.Default.Check, contentDescription = "Selected colour", tint = HamleysRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        repeat(3 - rowColors.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                TextButton(onClick = { dismissKeyboard(); showCustom = true }, modifier = Modifier.height(48.dp)) {
                    Text("+  Custom colour", color = HamleysRed, fontWeight = FontWeight.SemiBold)
                }
                if (selectedColor != null) {
                    Text("Selected: ${selectedColor.name}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("T-shirt pattern · Optional", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Plain", "Striped", "Printed").forEach { item ->
                        val chosen = pattern == item
                        Surface(
                            modifier = Modifier.weight(1f).height(48.dp).clickable { dismissKeyboard(); pattern = item },
                            color = if (chosen) HamleysRed else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (chosen) HamleysRed else Color(0xFFE6DACE))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item, color = if (chosen) Color.White else Ink, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            Divider(color = Color(0xFFE8DCCF))
            Row(
                Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text("Cancel", color = Ink, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { selectedColor?.let { onStart(name, it, pattern) } },
                    enabled = name.isNotBlank() && selectedColor != null,
                    modifier = Modifier.weight(1.5f).height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HamleysRed)
                ) { Text("Start play · 30 min", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showCustom) {
        CustomColorDialog(
            onDismiss = { showCustom = false },
            onSelect = {
                selectedColorName = it.name
                selectedColorHex = it.hex
                showCustom = false
                dismissKeyboard()
            }
        )
    }
}

@Composable
private fun CustomColorDialog(onDismiss: () -> Unit, onSelect: (ShirtColor) -> Unit) {
    var hue by remember { mutableFloatStateOf(35f) }
    var saturation by remember { mutableFloatStateOf(0.8f) }
    var brightness by remember { mutableFloatStateOf(0.9f) }
    val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))
    val hex = String.format("#%06X", 0xFFFFFF and colorInt)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a custom colour", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(12.dp)).background(Color(colorInt)))
                Spacer(Modifier.height(12.dp))
                Text("Hue")
                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
                Text("Saturation")
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)
                Text("Brightness")
                Slider(value = brightness, onValueChange = { brightness = it }, valueRange = 0f..1f)
                Text(hex, color = Muted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = { onSelect(ShirtColor("Custom $hex", hex)) }, colors = ButtonDefaults.buttonColors(containerColor = HamleysRed)) {
                Text("Use colour")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = Cream
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(76.dp).clip(CircleShape).background(Color(0xFFF2E5D5)), contentAlignment = Alignment.Center) {
            Text("✦", color = HamleysGold, fontSize = 36.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = Muted, fontSize = 13.sp)
    }
}

private fun parseColor(hex: String): Color = try {
    Color(AndroidColor.parseColor(hex))
} catch (_: Exception) {
    HamleysGold
}

private fun formatClock(time: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time)).lowercase(Locale.getDefault())

private fun formatDate(time: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(time))

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1_000L
    return "%02d:%02d".format(Locale.getDefault(), seconds / 60, seconds % 60)
}
