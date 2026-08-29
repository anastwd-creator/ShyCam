package com.shycam.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shycam.app.model.RecordingStatus
import com.shycam.app.ui.theme.CameraBlack
import com.shycam.app.ui.theme.RecordRed
import com.shycam.app.ui.theme.TextPrimary
import com.shycam.app.util.MediaUtils

@Composable
fun RecordingTimerBadge(
  durationSeconds: Long,
  recordingStatus: RecordingStatus,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
  val dotAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot_alpha"
  )

  val isPaused = recordingStatus == RecordingStatus.PAUSED

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(CameraBlack.copy(alpha = 0.8f))
      .border(1.dp, if (isPaused) TextPrimary.copy(alpha = 0.3f) else RecordRed.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag("recording_timer_badge"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(if (isPaused) TextPrimary.copy(alpha = 0.5f) else RecordRed)
        .alpha(if (isPaused) 1f else dotAlpha)
    )

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = if (isPaused) "PAUSED ${MediaUtils.formatDuration(durationSeconds)}" else "REC ${MediaUtils.formatDuration(durationSeconds)}",
      color = TextPrimary,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.sp
    )
  }
}
