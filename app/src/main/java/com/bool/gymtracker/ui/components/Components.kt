package com.bool.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymSurface
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun GymCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GymSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content,
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GymLime, contentColor = GymBlack),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium.copy(color = GymBlack))
    }
}

@Composable
fun EmptyState(title: String, body: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        if (actionLabel != null && onAction != null) {
            PrimaryButton(actionLabel, onAction)
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

fun formatDateTime(epochMs: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMs))
