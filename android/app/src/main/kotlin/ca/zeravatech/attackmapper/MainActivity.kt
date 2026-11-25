package ca.zeravatech.attackmapper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import ca.zeravatech.attackmapper.ui.AttackMapperApp

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent { AttackMapperApp(vm, onShare = ::shareJson) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** Text shared from another app, or selected text via "Attack Mapper" in the selection menu. */
    private fun handle(intent: Intent?) {
        val text = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        }
        vm.offerImport(text)
    }

    private fun shareJson() {
        if (vm.conversation.isEmpty) return
        val s = Strings.of(vm.lang)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, s.exportSubject)
            putExtra(Intent.EXTRA_TEXT, vm.exportJson())
        }
        startActivity(Intent.createChooser(send, s.export))
    }
}
