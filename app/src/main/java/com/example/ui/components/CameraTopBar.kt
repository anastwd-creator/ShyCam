package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OverlayVideoState
import com.example.model.RecordingStatus
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.OverlayCyan
import com.example.ui.theme.OverlayCyanMuted
import com.example.ui.theme.RecordRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CameraTopBar(
  isTorchOn: Boolean,
  isAudioRecordingEnabled: Boolean,
  isBackCamera: Boolean,
  showGrid: Boolean,
  overlayState: OverlayVideoState,
  recordingStatus: RecordingStatus,
  onToggleTorch: () -> Unit,
  onToggleAudioRecording: () -> Unit,
  onToggleCameraLens: () -> Unit,
  onToggleGrid: () -> Unit,
  onOpenOverlaySettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(GlassOverlay)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Flash / Torch Toggle
    IconButton(
      onClick = onToggleTorch,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if (isTorchOn) OverlayCyanMuted else Color.Transparent)
        .testTag("torch_toggle_button")
    ) {
      Icon(
        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
        contentDescription = "Toggle Torch",
        tint = if (isTorchOn) OverlayCyan else TextPrimary,
        modifier = Modifier.size(22.dp)
      )
    }

    // Audio recording microphone toggle
    IconButton(
      onClick = onToggleAudioRecording,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if (!isAudioRecordingEnabled) RecordRed.copy(alpha = 0.2f) else Color.Transparent)
        .testTag("audio_toggle_button")
    ) {
      Icon(
        imageVector = if (isAudioRecordingEnabled) Icons.Default.Mic else Icons.Default.MicOff,
        contentDescription = "Toggle Audio Recording",
        tint = if (isAudioRecordingEnabled) TextPrimary else RecordRed,
        modifier = Modifier.size(22.dp)
      )
    }

    // Grid toggle
    IconButton(
      onClick = onToggleGrid,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if (showGrid) OverlayCyanMuted else Color.Transparent)
        .testTag("grid_toggle_button")
    ) {
      Icon(
        imageVector = Icons.Default.GridOn,
        contentDescription = "Toggle Grid Lines",
        tint = if (showGrid) OverlayCyan else TextSecondary,
        modifier = Modifier.size(20.dp)
      )
    }

    // Active Overlay Status Badge
    if (overlayState.uri != null) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(OverlayCyan.copy(alpha = 0.15f))
          .border(1.dp, OverlayCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
          .clickable(onClick = onOpenOverlaySettings)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(OverlayCyan)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "OVERLAY ON",
          color = OverlayCyan,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }

    // Flip Camera Front/Back
    IconButton(
      onClick = onToggleCameraLens,
      enabled = recordingStatus == RecordingStatus.IDLE,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .testTag("flip_camera_button")
    ) {
      Icon(
        imageVector = Icons.Default.Cameraswitch,
        contentDescription = "Switch Camera Front/Back",
        tint = if (recordingStatus == RecordingStatus.IDLE) TextPrimary else TextSecondary.copy(alpha = 0.4f),
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
