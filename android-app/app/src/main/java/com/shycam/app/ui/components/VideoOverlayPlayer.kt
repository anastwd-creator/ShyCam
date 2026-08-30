package com.shycam.app.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shycam.app.R
import com.shycam.app.model.OverlayPreset
import com.shycam.app.model.OverlayVideoState
import com.shycam.app.ui.theme.CameraBlack
import com.shycam.app.ui.theme.CameraBorder
import com.shycam.app.ui.theme.CameraSurfaceElevated
import com.shycam.app.ui.theme.GlassOverlay
import com.shycam.app.ui.theme.OverlayCyan
import com.shycam.app.ui.theme.OverlayCyanMuted
import com.shycam.app.ui.theme.RecordRed
import com.shycam.app.ui.theme.TextPrimary
import com.shycam.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun VideoOverlayPlayer(
  overlayState: OverlayVideoState,
  onUpdateOpacity: (Float) -> Unit,
  onUpdateScale: (Float) -> Unit,
  onUpdateOffset: (Float, Float) -> Unit,
  onResetTransform: () -> Unit,
  onToggleLock: () -> Unit,
  onToggleVisibility: () -> Unit,
  onToggleControls: () -> Unit,
  onOpenSettings: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uri = overlayState.uri ?: return
  if (!overlayState.isVisible) return

  val context = LocalContext.current
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
  var isPrepared by remember { mutableStateOf(false) }

  // Clean up MediaPlayer on disposal or URI change
  DisposableEffect(uri) {
    onDispose {
      try {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
      } catch (_: Exception) {}
    }
  }

  // Handle Playback speed changes
  LaunchedEffect(overlayState.playbackSpeed, isPrepared) {
    if (isPrepared && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        mediaPlayer?.playbackParams = PlaybackParams().apply {
          speed = overlayState.playbackSpeed
          pitch = 1.0f
        }
      } catch (_: Exception) {}
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val containerWidth = maxWidth
    val containerHeight = maxHeight

    // Compute layout styling based on preset
    when (overlayState.preset) {
      OverlayPreset.SPLIT_TOP -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.TopCenter)
            .alpha(overlayState.opacity)
            .background(Color.Transparent)
            .border(1.dp, Color.Transparent)
        ) {
          OverlayVideoSurface(
            uri = uri,
            onMediaPlayerReady = { mp ->
              mediaPlayer = mp
              isPrepared = true
            }
          )
          OverlayHUDHeader(
            overlayState = overlayState,
            onToggleLock = onToggleLock,
            onToggleControls = onToggleControls,
            onOpenSettings = onOpenSettings,
            onClose = onClose,
            modifier = Modifier.align(Alignment.TopCenter)
          )
        }
      }

      OverlayPreset.SPLIT_BOTTOM -> {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .align(Alignment.BottomCenter)
            .alpha(overlayState.opacity)
            .background(Color.Transparent)
            .border(1.dp, Color.Transparent)
        ) {
          OverlayVideoSurface(
            uri = uri,
            onMediaPlayerReady = { mp ->
              mediaPlayer = mp
              isPrepared = true
            }
          )
          OverlayHUDHeader(
            overlayState = overlayState,
            onToggleLock = onToggleLock,
            onToggleControls = onToggleControls,
            onOpenSettings = onOpenSettings,
            onClose = onClose,
            modifier = Modifier.align(Alignment.TopCenter)
          )
        }
      }

      OverlayPreset.FULL_SCREEN, OverlayPreset.GHOST_GUIDE -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .alpha(overlayState.opacity)
            .background(Color.Transparent)
        ) {
          OverlayVideoSurface(
            uri = uri,
            onMediaPlayerReady = { mp ->
              mediaPlayer = mp
              isPrepared = true
            }
          )
          OverlayHUDHeader(
            overlayState = overlayState,
            onToggleLock = onToggleLock,
            onToggleControls = onToggleControls,
            onOpenSettings = onOpenSettings,
            onClose = onClose,
            modifier = Modifier.align(Alignment.TopCenter)
          )
        }
      }

      OverlayPreset.PIP_CORNER -> {
        // Floating PiP with pinch-to-zoom and drag
        val baseWidth = (containerWidth * 0.45f) * overlayState.scale
        val shape = RoundedCornerShape(16.dp)

        Box(
          modifier = Modifier
            .offset {
              IntOffset(
                x = overlayState.offsetX.roundToInt(),
                y = overlayState.offsetY.roundToInt()
              )
            }
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .width(baseWidth.coerceIn(120.dp, containerWidth * 0.85f))
            .aspectRatio(9f / 16f)
            .shadow(12.dp, shape)
            .clip(shape)
            .alpha(overlayState.opacity)
            .background(Color.Transparent)
            .border(1.5.dp, Color.Transparent, shape)
            .then(
              if (!overlayState.isLocked) {
                Modifier.pointerInput(Unit) {
                  detectTransformGestures { _, pan, zoom, _ ->
                    onUpdateOffset(pan.x, pan.y)
                    if (zoom != 1.0f) {
                      onUpdateScale(overlayState.scale * zoom)
                    }
                  }
                }
              } else Modifier
            )
            .testTag("video_overlay_pip")
        ) {
          OverlayVideoSurface(
            uri = uri,
            onMediaPlayerReady = { mp ->
              mediaPlayer = mp
              isPrepared = true
            }
          )

          // Top Header Overlay Badge
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.Transparent)
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onToggleLock,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = if (overlayState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                  contentDescription = stringResource(R.string.btn_lock_overlay_pos),
                  tint = if (overlayState.isLocked) TextPrimary else TextSecondary,
                  modifier = Modifier.size(14.dp)
                )
              }

              IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = stringResource(R.string.btn_overlay_settings),
                  tint = TextSecondary,
                  modifier = Modifier.size(14.dp)
                )
              }

              IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.btn_close_overlay),
                  tint = RecordRed,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OverlayVideoSurface(
  uri: Uri,
  onMediaPlayerReady: (MediaPlayer) -> Unit
) {
  val context = LocalContext.current

  AndroidView(
    factory = { ctx ->
      TextureView(ctx).apply {
        var activeSurface: Surface? = null
        var localMp: MediaPlayer? = null

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
          override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
            try {
              localMp?.release()
              activeSurface?.release()

              val surface = Surface(st)
              activeSurface = surface

              val mp = MediaPlayer().apply {
                setDataSource(context, uri)
                setSurface(surface)
                isLooping = true
                // CRITICAL REQUIREMENT: Display video loop WITHOUT AUDIO
                setVolume(0f, 0f)
                setOnPreparedListener { player ->
                  try {
                    player.start()
                    onMediaPlayerReady(player)
                  } catch (_: Exception) {}
                }
                prepareAsync()
              }
              localMp = mp
            } catch (_: Exception) {}
          }

          override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}

          override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
            try {
              localMp?.stop()
              localMp?.release()
              localMp = null
              activeSurface?.release()
              activeSurface = null
            } catch (_: Exception) {}
            return true
          }

          override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  )
}

@Composable
private fun OverlayHUDHeader(
  overlayState: OverlayVideoState,
  onToggleLock: () -> Unit,
  onToggleControls: () -> Unit,
  onOpenSettings: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(Color.Transparent)
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = overlayState.title.ifEmpty { stringResource(R.string.reference_video) },
        color = TextSecondary,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = stringResource(R.string.btn_overlay_settings),
          tint = TextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onClose,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = stringResource(R.string.btn_close_overlay),
          tint = RecordRed,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
