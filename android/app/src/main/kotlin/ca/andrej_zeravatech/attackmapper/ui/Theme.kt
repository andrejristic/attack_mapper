package ca.andrej_zeravatech.attackmapper.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ca.andrej_zeravatech.attackmapper.core.AlertLevel
import ca.andrej_zeravatech.attackmapper.core.RiskBand

private val Navy = Color(0xFF0F1B3D)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E40AF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FF),
    onPrimaryContainer = Navy,
    secondaryContainer = Color(0xFFE2E8F0),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFF7F8FC),
    surfaceVariant = Color(0xFFE7EAF3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93B4FF),
    onPrimary = Navy,
    primaryContainer = Color(0xFF1E2E5C),
    onPrimaryContainer = Color(0xFFDBE4FF),
    secondaryContainer = Color(0xFF263042),
    background = Color(0xFF0B1220),
    surface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFF1A2335),
)

object RiskColors {
    val low = Color(0xFF16A34A)
    val medium = Color(0xFFF59E0B)
    val high = Color(0xFFDC2626)
    val similarity = Color(0xFF8B5CF6)

    fun band(b: RiskBand) = when (b) {
        RiskBand.LOW -> low
        RiskBand.MEDIUM -> medium
        RiskBand.HIGH -> high
    }

    fun level(l: AlertLevel) = when (l) {
        AlertLevel.SAFE -> low
        AlertLevel.MONITOR -> medium
        AlertLevel.HIGH -> high
    }
}

@Composable
fun AttackMapperTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
