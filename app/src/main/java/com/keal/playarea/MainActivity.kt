package com.keal.playarea

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HamleysRed = Color(0xFFC8102E)
private val HamleysGold = Color(0xFFD6A84F)
private val Cream = Color(0xFFFFF7EC)
private val Ink = Color(0xFF30251F)
private val Muted = Color(0xFF887A70)

private data class ShirtColor(val name: String, val hex: String) {
    val color: Color get() = Color(AndroidColor.parseColor(hex))
}

private val shirtColors = listOf(
    ShirtColor("Red", "#E53935"),
    ShirtColor("Maroon", "#800000"),
    ShirtColor("Orange", "#F57C00"),
    ShirtColor("Peach", "#FFAB91"),
    ShirtColor("Yellow", "#FFEB3B"),
    ShirtColor("Mustard Yellow", "#D6A21E"),
    ShirtColor("Lime", "#C0CA33"),
    ShirtColor("Green", "#2E7D32"),
    ShirtColor("Olive", "#737B25"),
    ShirtColor("Mint", "#80CBC4"),
    ShirtColor("Teal", "#00897B"),
    ShirtColor("Sky Blue", "#4FC3F7"),
    ShirtColor("Blue", "#1E88E5"),
    ShirtColor("Navy", "#172A53"),
    ShirtColor("Purple", "#7E57C2"),
    ShirtColor("Lavender", "#B39DDB"),
    ShirtColor("Pink", "#EC407A"),
    ShirtColor("Hot Pink", "#D81B60"),
    ShirtColor("White", "#FFFFFF"),
    ShirtColor("Black", "#242424"),
    ShirtColor("Grey", "#9E9E9E"),
    ShirtColor("Brown", "#795548"),
    ShirtColor("Beige", "#D7C3A5"),
    ShirtColor("Turquoise", "#26A69A")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SessionAlarmReceiver.ensureChannel(this)
        setContent {
            PlayAreaTheme {
                PlayAreaScreen(
                    onStartSession = { session ->
                        SessionStore.add(this, session)
                        SessionAlarmReceiver.schedule(this, session)
                    },
                    onEndSession = { id ->
                        SessionAlarmReceiver.cancel(this, id)
                        SessionStore.finish(this, id)
                    }
                )
            }
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
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
private fun PlayAreaScreen(
    onStartSession: (PlaySession) -> Unit,
    onEndSession: (Long) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var sessions by remember { mutableStateOf(SessionStore.getAll(context)) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf("Active") }
    var showSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showSheet) {
        if (showSheet) focusManager.clearFocus(force = true)
    }

    val active = remember(sessions) {
        sessions.filter { it.finishedAt == null }.sortedBy { it.endsAt }
    }
    val previous = remember(sessions) {
        sessions.filter { it.finishedAt != null }.sortedByDescending { it.finishedAt ?: 0L }
    }
    val visibleActive = remember(active, query) {
        active.filter { it.childName.contains(query, true) || it.colorName.contains(query, true) }
    }
    val visiblePrevious = remember(previous, query) {
        previous.filter { it.childName.contains(query, true) || it.colorName.contains(query, true) }
    }

    Scaffold(
        containerColor = Cream,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    focusManager.clearFocus(force = true)
                    showSheet = true
                },
                containerColor = HamleysRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add child", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PLAY AREA", color = HamleysRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
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
                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().focusProperties { canFocus = !showSheet },
                placeholder = { Text("Search name or t-shirt color") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.clickable { query = "" }.padding(8.dp))
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0E6D9)).padding(4.dp)) {
                listOf("Active", "Previous").forEach { tab ->
                    val chosen = selectedTab == tab
                    Box(
                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(11.dp))
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
            if (selectedTab == "Active") {
                if (visibleActive.isEmpty()) {
                    EmptyState(
                        title = if (query.isBlank()) "Ready for some fun?" else "No matching sessions",
                        subtitle = if (query.isBlank()) "Tap + when a child is ready to play." else "Try another name or color."
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(visibleActive, key = { it.id }) { session ->
                            ActiveSessionCard(session) { onEndSession(session.id); sessions = SessionStore.getAll(context) }
                        }
                        item(key = "active-bottom-space") { Spacer(Modifier.height(84.dp)) }
                    }
                }
            } else {
                if (visiblePrevious.isEmpty()) {
                    EmptyState("No previous sessions yet", "Completed play sessions will appear here.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(visiblePrevious, key = { it.id }) { session -> PreviousSessionCard(session) }
                        item(key = "previous-bottom-space") { Spacer(Modifier.height(84.dp)) }
                    }
                }
            }
        }
    }

    if (showSheet) {
        AddChildSheet(
            onDismiss = { showSheet = false },
            onStart = { name, color, pattern ->
                val start = System.currentTimeMillis()
                val session = PlaySession(
                    id = start,
                    childName = name.trim(),
                    colorName = color.name,
                    colorHex = color.hex,
                    pattern = pattern,
                    startedAt = start,
                    endsAt = start + 30 * 60 * 1000L
                )
                onStartSession(session)
                sessions = SessionStore.getAll(context)
                showSheet = false
            }
        )
    }
}

@Composable
private fun ActiveSessionCard(session: PlaySession, onEnd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE1D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(parseColor(session.colorHex)), contentAlignment = Alignment.Center) {
                    Text(
                        session.childName.firstOrNull()?.uppercase() ?: "?",
                        color = if (session.colorHex.equals("#FFFFFF", true) || session.colorHex.equals("#FFEB3B", true)) Ink else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.childName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(parseColor(session.colorHex)))
                        Spacer(Modifier.width(5.dp))
                        Text(session.colorName, color = Muted, fontSize = 12.sp)
                        Text("  •  ${session.pattern}", color = Muted, fontSize = 12.sp)
                    }
                }
                SessionTimer(endTimeMillis = session.endsAt)
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
                    modifier = Modifier.height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HamleysRed)
                ) { Text("End Session", fontWeight = FontWeight.SemiBold) }
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
        overtime || remaining < 2 * 60 * 1_000L -> Color(0xFFC62828)
        remaining < 10 * 60 * 1_000L -> Color(0xFFEF7D22)
        else -> Color(0xFF2E7D50)
    }
    Column(horizontalAlignment = Alignment.End) {
        Text(if (overtime) "OVERTIME" else "TIME LEFT", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = timerColor)
        Text(
            if (overtime) "+${formatDuration(-remaining)}" else formatDuration(remaining),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
    }
}

@Composable
private fun PreviousSessionCard(session: PlaySession) {
    val end = session.finishedAt ?: session.endsAt
    val duration = ((end - session.startedAt).coerceAtLeast(0L) / 60000L).toInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE1D3))
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(parseColor(session.colorHex)))
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(session.childName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${session.colorName}  •  ${formatDate(session.startedAt)}", color = Muted, fontSize = 12.sp)
                Text("${formatClock(session.startedAt)} – ${formatClock(end)}", color = Muted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${duration} min", color = HamleysRed, fontWeight = FontWeight.Bold)
                Text("COMPLETED", fontSize = 9.sp, color = Muted, letterSpacing = 0.8.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChildSheet(
    onDismiss: () -> Unit,
    onStart: (String, ShirtColor, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedColorName by rememberSaveable { mutableStateOf("") }
    var selectedColorHex by rememberSaveable { mutableStateOf("") }
    var pattern by rememberSaveable { mutableStateOf("Plain") }
    var showCustom by rememberSaveable { mutableStateOf(false) }
    val selectedColor = if (selectedColorHex.isBlank()) null else ShirtColor(selectedColorName, selectedColorHex)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val screenHeightAtOpen = remember { LocalConfiguration.current.screenHeightDp.dp }
    val sheetHeight = screenHeightAtOpen * 0.92f
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissKeyboardAndFocus() {
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
                Modifier.padding(vertical = 10.dp).size(width = 42.dp, height = 4.dp),
                color = Color(0xFFD7C8B8),
                shape = CircleShape
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Text("New play session", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("A little play goes a long way.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Text("CHILD’S NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter child’s name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { dismissKeyboardAndFocus() }),
                    shape = RoundedCornerShape(13.dp)
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("T-SHIRT COLOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                    Spacer(Modifier.weight(1f))
                    Text("Required", color = HamleysRed, fontSize = 11.sp)
                }
                Spacer(Modifier.height(9.dp))

                shirtColors.chunked(3).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEach { swatch ->
                            val chosen = selectedColorHex.equals(swatch.hex, ignoreCase = true)
                            Surface(
                                modifier = Modifier.weight(1f).height(44.dp)
                                    .clickable {
                                        dismissKeyboardAndFocus()
                                        selectedColorName = swatch.name
                                        selectedColorHex = swatch.hex
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, if (chosen) HamleysRed else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(17.dp).clip(CircleShape).background(swatch.color))
                                    Spacer(Modifier.width(5.dp))
                                    Text(swatch.name, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f), color = Ink)
                                    Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                                        if (chosen) Icon(Icons.Default.Check, contentDescription = "Selected", tint = HamleysRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                        repeat(3 - rowColors.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                TextButton(
                    onClick = { dismissKeyboardAndFocus(); showCustom = true },
                    modifier = Modifier.height(44.dp)
                ) { Text("+  Custom color", color = HamleysRed, fontWeight = FontWeight.SemiBold) }

                Row(
                    modifier = Modifier.fillMaxWidth().height(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedColor != null) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = HamleysRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Selected: ${selectedColor.name}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("T-SHIRT PATTERN  •  OPTIONAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Plain", "Striped", "Printed").forEach { item ->
                        val chosen = pattern == item
                        Surface(
                            modifier = Modifier.weight(1f).height(44.dp).clickable {
                                dismissKeyboardAndFocus()
                                pattern = item
                            },
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
                modifier = Modifier.fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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
                ) { Text("Start Play · 30 min", fontWeight = FontWeight.Bold) }
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
                dismissKeyboardAndFocus()
            }
        )
    }
}

@Composable
private fun CustomColorDialog(onDismiss: () -> Unit, onSelect: (ShirtColor) -> Unit) {
    var hue by remember { mutableFloatStateOf(35f) }
    var saturation by remember { mutableFloatStateOf(0.8f) }
    var value by remember { mutableFloatStateOf(0.9f) }
    val hsv = floatArrayOf(hue, saturation, value)
    val colorInt = AndroidColor.HSVToColor(hsv)
    val hex = String.format("#%06X", 0xFFFFFF and colorInt)
    val color = Color(colorInt)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a custom color", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(12.dp)).background(color))
                Spacer(Modifier.height(12.dp))
                Text("Hue")
                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
                Text("Saturation")
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)
                Text("Brightness")
                Slider(value = value, onValueChange = { value = it }, valueRange = 0f..1f)
                Text(hex, color = Muted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = { onSelect(ShirtColor("Custom $hex", hex)) }, colors = ButtonDefaults.buttonColors(containerColor = HamleysRed)) {
                Text("Use color")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(76.dp).clip(CircleShape).background(Color(0xFFF2E5D5)),
            contentAlignment = Alignment.Center
        ) {
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
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time))
        .lowercase(Locale.getDefault())

private fun formatDate(time: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(time))

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = seconds / 60
    val remainder = seconds % 60
    return "%02d:%02d".format(Locale.getDefault(), minutes, remainder)
}