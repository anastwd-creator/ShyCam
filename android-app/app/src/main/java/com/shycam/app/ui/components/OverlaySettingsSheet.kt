package com.shycam.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shycam.app.R
import com.shycam.app.model.OverlayPreset
import com.shycam.app.model.OverlayVideoState
import com.shycam.app.ui.theme.CameraBlack
import com.shycam.app.ui.theme.CameraBorder
import com.shycam.app.ui.theme.CameraSurfaceDark
import com.shycam.app.ui.theme.CameraSurfaceElevated
import com.shycam.app.ui.theme.OverlayCyan
import com.shycam.app.ui.theme.OverlayCyanMuted
import com.shycam.app.ui.theme.RecordRed
import com.shycam.app.ui.theme.TextPrimary
import com.shycam.app.ui.theme.TextSecondary
import com.shycam.app.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OverlaySettingsSheet(
  overlayState: OverlayVideoState,
  onDismiss: () -> Unit,
  onSelectPreset: (OverlayPreset) -> Unit,
  onUpdateOpacity: (Float) -> Unit,
  onUpdateScale: (Float) -> Unit,
  onUpdateSpeed: (Float) -> Unit,
  onResetPosition: () -> Unit,
  onToggleLock: () -> Unit,
  onToggleVisibility: () -> Unit,
  onReplaceVideo: () -> Unit,
  onRemoveOverlay: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CameraSurfaceDark,
    contentColor = TextPrimary,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .size(width = 36.dp, height = 4.dp)
          .clip(CircleShape)
          .background(CameraBorder)
      )
    },
    modifier = modifier.testTag("overlay_settings_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header with Video Title and Close
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.overlay_sheet_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = overlayState.title.ifEmpty { stringResource(R.string.imported_gallery_video) },
            fontSize = 12.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.btn_close),
            tint = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Silent Loop Notice Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(OverlayCyanMuted)
          .border(1.dp, OverlayCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(OverlayCyan)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = stringResource(R.string.overlay_loop_notice),
          color = TextPrimary,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Layout Preset Options
      Text(
        text = stringResource(R.string.section_layout_preset),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = TextTertiary
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OverlayPreset.values().forEach { preset ->
          val isSelected = overlayState.preset == preset
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) OverlayCyan else CameraSurfaceElevated)
              .border(
                1.dp,
                if (isSelected) OverlayCyan else CameraBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { onSelectPreset(preset) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = stringResource(preset.titleRes),
              color = if (isSelected) CameraBlack else TextPrimary,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Opacity / Ghosting Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.section_opacity),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = TextTertiary
        )
        Text(
          text = "${(overlayState.opacity * 100).toInt()}%",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = OverlayCyan
        )
      }
      Slider(
        value = overlayState.opacity,
        onValueChange = onUpdateOpacity,
        valueRange = 0.1f..1.0f,
        colors = SliderDefaults.colors(
          thumbColor = OverlayCyan,
          activeTrackColor = OverlayCyan,
          inactiveTrackColor = CameraBorder
        )
      )

      // Scale Slider (if PiP mode)
      if (overlayState.preset == OverlayPreset.PIP_CORNER) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.section_size_scale),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextTertiary
          )
          Text(
            text = String.format("%.1fx", overlayState.scale),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = OverlayCyan
          )
        }
        Slider(
          value = overlayState.scale,
          onValueChange = onUpdateScale,
          valueRange = 0.4f..1.8f,
          colors = SliderDefaults.colors(
            thumbColor = OverlayCyan,
            activeTrackColor = OverlayCyan,
            inactiveTrackColor = CameraBorder
          )
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Playback Speed
      Text(
        text = stringResource(R.string.section_playback_speed),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = TextTertiary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.5f, 2.0f)
        speeds.forEach { speed ->
          val isSelected = overlayState.playbackSpeed == speed
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) OverlayCyan else CameraSurfaceElevated)
              .border(
                1.dp,
                if (isSelected) OverlayCyan else CameraBorder,
                RoundedCornerShape(8.dp)
              )
              .clickable { onUpdateSpeed(speed) }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${speed}x",
              color = if (isSelected) CameraBlack else TextPrimary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Actions Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = onResetPosition,
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
          border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CameraBorder))
        ) {
          Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = stringResource(R.string.btn_reset_pos), fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onToggleLock,
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (overlayState.isLocked) OverlayCyan else TextPrimary
          ),
          border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (overlayState.isLocked) OverlayCyan else CameraBorder)
          )
        ) {
          Icon(
            if (overlayState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = if (overlayState.isLocked) stringResource(R.string.btn_locked) else stringResource(R.string.btn_unlocked), fontSize = 12.sp)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilledTonalButton(
          onClick = {
            onReplaceVideo()
            onDismiss()
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = CameraSurfaceElevated,
            contentColor = TextPrimary
          )
        ) {
          Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = stringResource(R.string.btn_replace_video), fontSize = 12.sp)
        }

        Button(
          onClick = {
            onRemoveOverlay()
            onDismiss()
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(
            containerColor = RecordRed.copy(alpha = 0.18f),
            contentColor = RecordRed
          )
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = stringResource(R.string.btn_remove), fontSize = 12.sp)
        }
      }
    }
  }
}
