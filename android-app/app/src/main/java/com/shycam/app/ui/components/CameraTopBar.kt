package com.shycam.app.ui.components

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
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shycam.app.R
import com.shycam.app.model.OverlayVideoState
import com.shycam.app.model.RecordingStatus
import com.shycam.app.ui.theme.CameraBlack
import com.shycam.app.ui.theme.CameraSurfaceElevated
import com.shycam.app.ui.theme.OverlayCyan
import com.shycam.app.ui.theme.OverlayCyanMuted
import com.shycam.app.ui.theme.RecordRed
import com.shycam.app.ui.theme.TextPrimary
import com.shycam.app.ui.theme.TextSecondary

@Composable
fun CameraTopBar(
  isTorchOn: Boolean,
  isAudioRecordingEnabled: Boolean,
  isBackCamera: Boolean,
  overlayState: OverlayVideoState,
  recordingStatus: RecordingStatus,
  onToggleTorch: () -> Unit,
  onToggleAudioRecording: () -> Unit,
  onOpenOverlaySettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        contentDescription = stringResource(R.string.btn_toggle_torch),
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
        contentDescription = stringResource(R.string.btn_toggle_audio),
        tint = if (isAudioRecordingEnabled) TextPrimary else RecordRed,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
