package ca.andrej_zeravatech.attackmapper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.andrej_zeravatech.attackmapper.AppViewModel
import ca.andrej_zeravatech.attackmapper.Lang
import ca.andrej_zeravatech.attackmapper.ModelState
import ca.andrej_zeravatech.attackmapper.Strings
import ca.andrej_zeravatech.attackmapper.core.Examples

val LocalStrings = staticCompositionLocalOf { Strings.EN }

private enum class Dialog { NONE, PASTE, HISTORY, ABOUT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackMapperApp(vm: AppViewModel, onShare: () -> Unit) {
    val s = Strings.of(vm.lang)
    CompositionLocalProvider(LocalStrings provides s) {
        AttackMapperTheme {
            var dialog by remember { mutableStateOf(Dialog.NONE) }
            var menuOpen by remember { mutableStateOf(false) }
            var tab by rememberSaveable { mutableIntStateOf(0) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Attack Mapper", fontWeight = FontWeight.SemiBold) },
                        actions = {
                            LanguageSwitch(vm.lang, vm::setLanguage)
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = s.menu)
                                }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    val ready = vm.ready && !vm.busy
                                    DropdownMenuItem(text = { Text(s.exBenign) }, enabled = ready, onClick = {
                                        menuOpen = false; vm.load(Examples.BENIGN); tab = 2
                                    })
                                    DropdownMenuItem(text = { Text(s.exJailbreak) }, enabled = ready, onClick = {
                                        menuOpen = false; vm.load(Examples.JAILBREAK_SINGLE); tab = 2
                                    })
                                    DropdownMenuItem(text = { Text(s.exAttack) }, enabled = ready, onClick = {
                                        menuOpen = false; vm.load(Examples.ATTACK_MULTI); tab = 2
                                    })
                                    HorizontalDivider()
                                    DropdownMenuItem(text = { Text(s.paste) }, onClick = {
                                        menuOpen = false; dialog = Dialog.PASTE
                                    })
                                    DropdownMenuItem(text = { Text(s.history) }, onClick = {
                                        menuOpen = false; vm.refreshHistory(); dialog = Dialog.HISTORY
                                    })
                                    DropdownMenuItem(text = { Text(s.export) }, enabled = !vm.conversation.isEmpty,
                                        onClick = { menuOpen = false; onShare() })
                                    DropdownMenuItem(text = { Text(s.newAnalysis) }, enabled = !vm.busy, onClick = {
                                        menuOpen = false; vm.clear(); tab = 0
                                    })
                                    HorizontalDivider()
                                    DropdownMenuItem(text = { Text(s.about) }, onClick = {
                                        menuOpen = false; dialog = Dialog.ABOUT
                                    })
                                }
                            }
                        },
                    )
                },
            ) { padding ->
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .imePadding(),
                ) {
                    ModelBanner(vm.modelState, vm.busy)
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        if (maxWidth >= 840.dp) {
                            // Wide (Chromebook / tablet): the three panes of the original app side by side.
                            Row(Modifier.fillMaxSize()) {
                                ConversationPane(vm, Modifier.weight(1f))
                                VerticalDivider()
                                MapPane(vm.conversation, Modifier.weight(1.3f))
                                VerticalDivider()
                                ThreatsPane(vm.conversation, vm.report, Modifier.weight(1f))
                            }
                        } else {
                            Column(Modifier.fillMaxSize()) {
                                TabRow(selectedTabIndex = tab) {
                                    listOf(s.tabConversation, s.tabMap, s.tabThreats).forEachIndexed { i, title ->
                                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
                                    }
                                }
                                when (tab) {
                                    0 -> ConversationPane(vm, Modifier.fillMaxSize())
                                    1 -> MapPane(vm.conversation, Modifier.fillMaxSize())
                                    else -> ThreatsPane(vm.conversation, vm.report, Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }

            vm.pendingImport?.let { p ->
                ImportDialog(p, ready = vm.ready, onFlip = vm::flipImportRole,
                    onConfirm = { vm.confirmImport(); tab = 2 }, onDismiss = vm::dismissImport)
            }
            when (dialog) {
                Dialog.PASTE -> PasteDialog(onNext = { dialog = Dialog.NONE; vm.offerImport(it) },
                    onDismiss = { dialog = Dialog.NONE })
                Dialog.HISTORY -> HistoryDialog(vm.historyEntries,
                    onOpen = { dialog = Dialog.NONE; vm.openHistory(it); tab = 2 },
                    onDelete = vm::deleteHistory, onDismiss = { dialog = Dialog.NONE })
                Dialog.ABOUT -> AboutDialog(onDismiss = { dialog = Dialog.NONE })
                Dialog.NONE -> Unit
            }
        }
    }
}

@Composable
private fun LanguageSwitch(current: Lang, onChange: (Lang) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Lang.entries.forEach { l ->
            val selected = l == current
            TextButton(onClick = { onChange(l) }, modifier = Modifier.width(48.dp)) {
                Text(
                    l.code.uppercase(),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun ModelBanner(state: ModelState, busy: Boolean) {
    val s = LocalStrings.current
    when (state) {
        ModelState.Loading -> Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(s.loadingModel, style = MaterialTheme.typography.bodyMedium)
        }
        is ModelState.Failed -> Text(
            "${s.modelFailed}: ${state.message}",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp),
        )
        ModelState.Ready -> if (busy) LinearProgressIndicator(Modifier.fillMaxWidth()) else Spacer(Modifier.size(0.dp))
    }
}
