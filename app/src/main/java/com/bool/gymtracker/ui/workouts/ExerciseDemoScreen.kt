package com.bool.gymtracker.ui.workouts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bool.gymtracker.domain.ExerciseDemo
import com.bool.gymtracker.domain.ExerciseDemos
import com.bool.gymtracker.domain.ExerciseTargetMuscles
import com.bool.gymtracker.domain.MuscleGroup
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymSurfaceHigh
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.delay

private const val FrameMs = 700L

fun loadAssetBitmap(path: String, context: android.content.Context): Bitmap? =
    runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it) } }.getOrNull()

@Composable
fun ExerciseDemoThumbnail(name: String, modifier: Modifier = Modifier) {
    val demo = ExerciseDemos.forName(name)
    val context = LocalContext.current
    val bitmap = remember(demo?.lockoutAsset) {
        demo?.let { loadAssetBitmap(it.lockoutAsset, context) }
    }
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GymSurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "$name demo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun ExerciseDemoLoop(demo: ExerciseDemo, modifier: Modifier = Modifier, contentDescription: String) {
    val context = LocalContext.current
    val lockout = remember(demo.lockoutAsset) { loadAssetBitmap(demo.lockoutAsset, context) }
    val bottom = remember(demo.bottomAsset) { loadAssetBitmap(demo.bottomAsset, context) }
    var showLockout by remember(demo) { mutableStateOf(true) }
    LaunchedEffect(demo) {
        if (lockout == null || bottom == null) return@LaunchedEffect
        while (true) {
            delay(FrameMs)
            showLockout = !showLockout
        }
    }
    val frame = if (showLockout) lockout else bottom
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GymSurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (frame != null) {
            Image(
                bitmap = frame.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text("No demo yet", color = GymMuted, modifier = Modifier.padding(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDemoScreen(
    name: String,
    muscleGroup: MuscleGroup,
    onBack: () -> Unit,
    onAdd: (() -> Unit)? = null,
) {
    val demo = ExerciseDemos.forName(name)
    val targets = ExerciseTargetMuscles.forName(name, muscleGroup)
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (demo != null) {
                    ExerciseDemoLoop(
                        demo = demo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentDescription = "$name animation",
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GymSurfaceHigh)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No demo yet", color = GymMuted)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Targeted muscles", style = MaterialTheme.typography.titleMedium, color = GymText)
                    Text(
                        "Main: ${targets.primary.joinToString()}",
                        color = GymText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Auxiliary: ${targets.auxiliary.joinToString().ifBlank { "None listed" }}",
                        color = GymMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How to", style = MaterialTheme.typography.titleMedium, color = GymText)
                    if (demo == null) {
                        Text("Tips will appear when this lift has a demo.", color = GymMuted)
                    } else {
                        demo.tips.forEach { tip ->
                            Text("• $tip", color = GymText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (onAdd != null) {
                PrimaryButton("Add", onAdd)
            }
        }
    }
}
