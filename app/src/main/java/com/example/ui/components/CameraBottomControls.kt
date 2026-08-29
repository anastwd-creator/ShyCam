package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.OverlayCyan
import com.example.ui.theme.OverlayCyanMuted
import com.example.ui.theme.RecordRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CameraBottomControls(
  recordingStatus: RecordingStatus,
  overlayState: OverlayVideoState,
  onRecordClick: () -> Unit,
  onPauseResumeClick: () -> Unit,
  onPickOverlayVideo: () -> Unit,
  onOpenOverlaySettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isRecording = recordingStatus == RecordingStatus.RECORDING || recordingStatus == RecordingStatus.PAUSED
  val isPaused = recordingStatus == RecordingStatus.PAUSED

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(GlassOverlay)
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left placeholder / spacer to keep shutter centered
      Spacer(modifier = Modifier.size(52.dp))

      // Middle: Main Record / Stop Shutter Button & Pause/Resume
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        // Pause / Resume button if actively recording
        AnimatedVisibility(visible = isRecording) {
          IconButton(
            onClick = onPauseResumeClick,
            modifier = Modifier
              .padding(end = 16.dp)
              .size(46.dp)
              .clip(CircleShape)
              .background(CameraSurfaceElevated)
              .border(1.dp, CameraBorder, CircleShape)
              .testTag("pause_resume_button")
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.MicOff,
              contentDescription = if (isPaused) "Resume recording" else "Pause recording",
              tint = TextPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // Shutter Button with Red Ring and White/Red Styling
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(RecordRed)
            .border(
              width = 3.dp,
              color = RecordRed,
              shape = CircleShape
            )
            .clickable(onClick = onRecordClick)
            .testTag("record_shutter_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = if (isRecording) "Stop recording" else "Record video",
            tint = Color.White,
            modifier = Modifier.size(38.dp)
          )
        }

        // Spacer to balance pause button on the left when recording
        if (isRecording) {
          Spacer(modifier = Modifier.width(62.dp))
        }
      }

      // Right button: Import Video from Gallery / Configure Overlay
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
          if (overlayState.uri != null) {
            onOpenOverlaySettings()
          } else {
            onPickOverlayVideo()
          }
        }
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (overlayState.uri != null) OverlayCyanMuted else CameraSurfaceElevated)
            .border(
              1.5.dp,
              if (overlayState.uri != null) OverlayCyan else CameraBorder,
              RoundedCornerShape(14.dp)
            )
            .testTag("overlay_picker_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Import reference overlay video from gallery",
            tint = if (overlayState.uri != null) OverlayCyan else TextPrimary,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (overlayState.uri != null) "Overlay" else "Import Ref",
          color = if (overlayState.uri != null) OverlayCyan else TextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
