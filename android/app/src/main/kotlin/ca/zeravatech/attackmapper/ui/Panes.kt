package ca.zeravatech.attackmapper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.zeravatech.attackmapper.AppViewModel
import ca.zeravatech.attackmapper.core.Conversation
import ca.zeravatech.attackmapper.core.FLAGGED_TURN_MIN
import ca.zeravatech.attackmapper.core.RiskBand
import ca.zeravatech.attackmapper.core.RiskReport
import ca.zeravatech.attackmapper.core.Role
import ca.zeravatech.attackmapper.core.Turn
import ca.zeravatech.attackmapper.core.AlertLevel
import java.util.Locale

internal fun fmt(d: Double): String = String.format(Locale.US, "%.2f", d)

// ---------------------------------------------------------------- conversation

@Composable
fun ConversationPane(vm: AppViewModel, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    var role by rememberSaveable { mutableStateOf(Role.USER) }
    var text by rememberSaveable { mutableStateOf("") }
    val conv = vm.conversation

    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = role == Role.USER, onClick = { role = Role.USER },
                            label = { Text("👤 ${s.roleUser}") })
                        FilterChip(selected = role == Role.ASSISTANT, onClick = { role = Role.ASSISTANT },
                            label = { Text("🤖 ${s.roleAssistant}") })
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(s.messageHint) },
                        minLines = 2,
                        maxLines = 6,
                    )
                    Button(
                        onClick = {
                            vm.addTurn(role, text)
                            text = ""
                            role = if (role == Role.USER) Role.ASSISTANT else Role.USER
                        },
                        enabled = vm.ready && !vm.busy && text.isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.addTurn)
                    }
                }
            }
        }
        if (conv.isEmpty) {
            item { Text(s.empty, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp)) }
        } else {
            item { Text(s.transcript, style = MaterialTheme.typography.titleMedium) }
            items(conv.turns, key = { it.id }) { TurnCard(it) }
        }
    }
}

@Composable
private fun TurnCard(t: Turn) {
    val s = LocalStrings.current
    val isUser = t.role == Role.USER
    val container = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    Card(colors = CardDefaults.cardColors(containerColor = container), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (isUser) "👤 " + s.roleUser else "🤖 " + s.roleAssistant) + "  ·  ${s.turn} ${t.id}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.weight(1f))
                RiskDot(t.riskScore)
                Spacer(Modifier.width(6.dp))
                Text(fmt(t.riskScore), style = MaterialTheme.typography.labelMedium)
            }
            Text("(${s.intent(t.intent)})", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(t.content, style = MaterialTheme.typography.bodyMedium)
            if (t.entities.isNotEmpty()) {
                Text("${s.entities}: ${t.entities.joinToString()}", style = MaterialTheme.typography.labelSmall,
                    color = RiskColors.medium)
            }
        }
    }
}

@Composable
private fun RiskDot(risk: Double, size: Int = 10) {
    Box(Modifier.size(size.dp).background(RiskColors.band(RiskBand.of(risk)), CircleShape))
}

// ---------------------------------------------------------------- map

@Composable
fun MapPane(conv: Conversation, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(s.graph, style = MaterialTheme.typography.titleMedium)
        if (conv.isEmpty) {
            Text(s.empty, style = MaterialTheme.typography.bodyMedium)
            return@Column
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Legend(RiskColors.low, s.legendLow)
            Legend(RiskColors.medium, s.legendMedium)
            Legend(RiskColors.high, s.legendHigh)
            Legend(RiskColors.similarity, s.legendSimilarity)
        }
        Row(Modifier.fillMaxWidth()) {
            Text("👤 ${s.roleUser}", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.weight(1f))
            Text("🤖 ${s.roleAssistant}", style = MaterialTheme.typography.labelMedium)
        }
        ConversationGraph(conv)
        Text(s.timeline, style = MaterialTheme.typography.titleMedium)
        RiskTimeline(conv)
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

/** Users on the left lane, AI on the right; sequence edges solid, similarity edges dashed arcs. */
@Composable
private fun ConversationGraph(conv: Conversation) {
    val s = LocalStrings.current
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface)
    val lineColor = MaterialTheme.colorScheme.outline
    val ringColor = MaterialTheme.colorScheme.surface
    val rowH = 64.dp
    Canvas(Modifier.fillMaxWidth().height(rowH * conv.turns.size + 16.dp)) {
        val rowPx = rowH.toPx()
        val top = 8.dp.toPx() + rowPx / 2f
        val leftX = size.width * 0.10f
        val rightX = size.width * 0.90f
        fun center(i: Int) = Offset(if (conv.turns[i].role == Role.USER) leftX else rightX, top + i * rowPx)

        conv.similarityEdges.forEach { e ->
            val a = center(e.source)
            val b = center(e.target)
            val cx = when {
                a.x != b.x -> size.width / 2f
                a.x == leftX -> a.x + size.width * 0.16f + (e.target - e.source) * 4.dp.toPx()
                else -> a.x - size.width * 0.16f - (e.target - e.source) * 4.dp.toPx()
            }
            val path = Path().apply {
                moveTo(a.x, a.y)
                quadraticTo(cx, (a.y + b.y) / 2f, b.x, b.y)
            }
            drawPath(path, RiskColors.similarity.copy(alpha = 0.75f), style = Stroke(
                width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))))
        }
        for (i in 1 until conv.turns.size) {
            drawLine(lineColor, center(i - 1), center(i), strokeWidth = 2.dp.toPx())
        }
        val r = 11.dp.toPx()
        val maxLabel = (size.width * 0.32f).toInt().coerceAtLeast(1)
        conv.turns.forEachIndexed { i, t ->
            val c = center(i)
            drawCircle(RiskColors.band(RiskBand.of(t.riskScore)), radius = r, center = c)
            drawCircle(ringColor, radius = r, center = c, style = Stroke(2.dp.toPx()))
            val layout = measurer.measure(
                "${s.turn} ${t.id} · ${fmt(t.riskScore)}\n${s.intent(t.intent)}",
                style = labelStyle,
                constraints = Constraints(maxWidth = maxLabel),
            )
            val lx = if (t.role == Role.USER) c.x + r + 6.dp.toPx() else c.x - r - 6.dp.toPx() - layout.size.width
            drawText(layout, topLeft = Offset(lx, c.y - layout.size.height / 2f))
        }
    }
}

/** Risk per turn on a 0..1.1 axis with the 0.3 / 0.7 alert thresholds. */
@Composable
private fun RiskTimeline(conv: Conversation) {
    val measurer = rememberTextMeasurer()
    val axis = MaterialTheme.colorScheme.outline
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(170.dp)) {
        val left = 28.dp.toPx()
        val bottom = size.height - 16.dp.toPx()
        val topY = 8.dp.toPx()
        val w = size.width - left - 8.dp.toPx()
        fun y(v: Double) = (bottom - (v / 1.1) * (bottom - topY)).toFloat()
        val n = conv.turns.size
        fun x(i: Int) = left + if (n <= 1) w / 2f else w * i / (n - 1).toFloat()

        drawLine(axis, Offset(left, topY), Offset(left, bottom), strokeWidth = 1.dp.toPx())
        drawLine(axis, Offset(left, bottom), Offset(left + w, bottom), strokeWidth = 1.dp.toPx())
        listOf(0.3 to RiskColors.medium, 0.7 to RiskColors.high).forEach { (v, col) ->
            drawLine(col.copy(alpha = 0.6f), Offset(left, y(v)), Offset(left + w, y(v)),
                strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
        }
        listOf(0.0, 0.5, 1.0).forEach { v ->
            val l = measurer.measure(String.format(Locale.US, "%.1f", v), labelStyle)
            drawText(l, topLeft = Offset(left - l.size.width - 4.dp.toPx(), y(v) - l.size.height / 2f))
        }
        for (i in 1 until n) {
            drawLine(lineColor, Offset(x(i - 1), y(conv.turns[i - 1].riskScore)),
                Offset(x(i), y(conv.turns[i].riskScore)), strokeWidth = 2.dp.toPx())
        }
        conv.turns.forEachIndexed { i, t ->
            drawCircle(RiskColors.band(RiskBand.of(t.riskScore)), radius = 5.dp.toPx(), center = Offset(x(i), y(t.riskScore)))
            val l = measurer.measure("$i", labelStyle)
            drawText(l, topLeft = Offset(x(i) - l.size.width / 2f, bottom + 2.dp.toPx()))
        }
    }
}

// ---------------------------------------------------------------- threats

@Composable
fun ThreatsPane(conv: Conversation, report: RiskReport, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val levelColor = RiskColors.level(report.level)
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.14f)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.totalRisk, style = MaterialTheme.typography.labelLarge)
                Text(fmt(report.total), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = levelColor)
                Text(
                    when (report.level) {
                        AlertLevel.HIGH -> s.levelHigh
                        AlertLevel.MONITOR -> s.levelMonitor
                        AlertLevel.SAFE -> s.levelSafe
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = levelColor,
                )
            }
        }
        Text(s.components, style = MaterialTheme.typography.titleMedium)
        report.components.forEach { (name, v) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${s.detector(name)}: ${fmt(v)}", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { v.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = RiskColors.band(RiskBand.of(v)),
                )
            }
        }
        Text(s.flagged, style = MaterialTheme.typography.titleMedium)
        val flagged = conv.turns.filter { it.riskScore > FLAGGED_TURN_MIN }
        if (flagged.isEmpty()) {
            Text(s.noneFlagged, style = MaterialTheme.typography.bodyMedium)
        } else {
            flagged.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskDot(t.riskScore)
                    Spacer(Modifier.width(8.dp))
                    Text("${s.turn} ${t.id} (${s.intent(t.intent)}): ${fmt(t.riskScore)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
