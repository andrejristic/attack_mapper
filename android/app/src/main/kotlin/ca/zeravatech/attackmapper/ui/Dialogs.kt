package ca.zeravatech.attackmapper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.zeravatech.attackmapper.HistoryStore
import ca.zeravatech.attackmapper.PendingImport
import ca.zeravatech.attackmapper.core.Role
import java.text.DateFormat
import java.util.Date

@Composable
fun ImportDialog(
    p: PendingImport,
    ready: Boolean,
    onFlip: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.importTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (p.turns.isEmpty()) {
                    Text(s.importNothing)
                } else {
                    if (!p.usedMarkers) Text(s.importNoMarkers, style = MaterialTheme.typography.bodySmall)
                    Text(s.importTapRole, style = MaterialTheme.typography.bodySmall)
                    LazyColumn(Modifier.heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(p.turns) { i, (role, text) ->
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = { onFlip(i) },
                                    label = { Text(if (role == Role.USER) "👤 ${s.roleUser}" else "🤖 ${s.roleAssistant}") },
                                )
                                Text(text, maxLines = 3, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = ready && p.turns.isNotEmpty()) { Text(s.analyze) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@Composable
fun PasteDialog(onNext: (String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.paste) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(s.pasteHint) },
                minLines = 6,
                maxLines = 14,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = { onNext(text) }, enabled = text.isNotBlank()) { Text(s.next) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@Composable
fun HistoryDialog(
    entries: List<HistoryStore.Entry>,
    onOpen: (HistoryStore.Entry) -> Unit,
    onDelete: (HistoryStore.Entry) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.history) },
        text = {
            if (entries.isEmpty()) {
                Text(s.historyEmpty)
            } else {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(entries, key = { it.id }) { e ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpen(e) }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(e.title.ifBlank { "—" }, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("${df.format(Date(e.createdMillis))} · ${e.turns.size} ${s.turn.lowercase()}",
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { onDelete(e) }) {
                                Icon(Icons.Default.Delete, contentDescription = s.delete)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val ctx = LocalContext.current
    val version = runCatching {
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
    }.getOrNull() ?: "?"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attack Mapper $version") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(s.aboutText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
    )
}
