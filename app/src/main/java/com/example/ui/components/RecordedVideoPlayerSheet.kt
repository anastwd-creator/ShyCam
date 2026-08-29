package com.example.ui.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.RecordedVideo
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraSurfaceDark
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.OverlayCyan
import com.example.ui.theme.RecordRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.MediaUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordedVideoGallerySheet(
  recordedVideos: List<RecordedVideo>,
  selectedVideo: RecordedVideo?,
  onSelectVideo: (RecordedVideo?) -> Unit,
  onUseAsOverlay: (RecordedVideo) -> Unit,
  onDeleteVideo: (RecordedVideo) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = {
      onSelectVideo(null)
      onDismiss()
    },
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
    modifier = modifier.testTag("recorded_gallery_sheet")
  ) {
    if (selectedVideo != null) {
      // Full Video Player View
      VideoPlayerView(
        video = selectedVideo,
        onBack = { onSelectVideo(null) },
        onUseAsOverlay = {
          onUseAsOverlay(selectedVideo)
          onDismiss()
        },
        onDelete = {
          onDeleteVideo(selectedVideo)
          onSelectVideo(null)
        },
        onShare = {
          val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, selectedVideo.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          }
          context.startActivity(Intent.createChooser(shareIntent, "Share Recorded Video"))
        }
      )
    } else {
      // Library Grid View
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .padding(bottom = 32.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Recorded Videos",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "${recordedVideos.size} recordings available",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recordedVideos.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(CameraSurfaceElevated)
              .border(1.dp, CameraBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.VideocamOff,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No recorded videos yet",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap the red shutter button to start recording",
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(420.dp)
          ) {
            items(recordedVideos, key = { it.id }) { video ->
              RecordedVideoGridCard(
                video = video,
                onClick = { onSelectVideo(video) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecordedVideoGridCard(
  video: RecordedVideo,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CameraSurfaceElevated)
      .border(1.dp, CameraBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .background(CameraBlack)
    ) {
      AsyncImage(
        model = video.uri,
        contentDescription = video.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Play icon badge
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(36.dp)
          .clip(CircleShape)
          .background(CameraBlack.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      // Duration badge
      if (video.durationMs > 0) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CameraBlack.copy(alpha = 0.8f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
          Text(
            text = MediaUtils.formatDurationMs(video.durationMs),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = video.title,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${MediaUtils.formatDate(video.dateAdded)} · ${MediaUtils.formatFileSize(video.sizeBytes)}",
        color = TextTertiary,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
private fun VideoPlayerView(
  video: RecordedVideo,
  onBack: () -> Unit,
  onUseAsOverlay: () -> Unit,
  onDelete: () -> Unit,
  onShare: () -> Unit
) {
  val context = LocalContext.current
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
  var isPlaying by remember { mutableStateOf(false) }
  var currentPosition by remember { mutableIntStateOf(0) }
  var duration by remember { mutableIntStateOf(0) }

  // Update progress timer
  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      mediaPlayer?.let {
        try {
          if (it.isPlaying) {
            currentPosition = it.currentPosition
            duration = it.duration
          }
        } catch (_: Exception) {}
      }
      delay(300L)
    }
  }

  DisposableEffect(video.uri) {
    onDispose {
      try {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
      } catch (_: Exception) {}
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(bottom = 32.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back to list",
            tint = TextPrimary
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = video.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.width(180.dp)
        )
      }

      Row {
        IconButton(onClick = onShare) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = OverlayCyan
          )
        }
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = RecordRed
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Video Player Frame
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(RoundedCornerShape(16.dp))
        .background(Color.Black)
        .border(1.dp, CameraBorder, RoundedCornerShape(16.dp))
    ) {
      AndroidView(
        factory = { ctx ->
          SurfaceView(ctx).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
              override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                  val mp = MediaPlayer().apply {
                    setDataSource(context, video.uri)
                    setDisplay(holder)
                    setOnPreparedListener { player ->
                      duration = player.duration
                      player.start()
                      isPlaying = true
                    }
                    setOnCompletionListener {
                      isPlaying = false
                      currentPosition = duration
                    }
                    prepareAsync()
                  }
                  mediaPlayer = mp
                } catch (_: Exception) {}
              }

              override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

              override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                  mediaPlayer?.stop()
                  mediaPlayer?.release()
                  mediaPlayer = null
                } catch (_: Exception) {}
              }
            })
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      // Center play/pause overlay trigger
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable {
            mediaPlayer?.let { mp ->
              if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
              } else {
                mp.start()
                isPlaying = true
              }
            }
          },
        contentAlignment = Alignment.Center
      ) {
        if (!isPlaying) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(CameraBlack.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Scrubber / Position Slider
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = MediaUtils.formatDurationMs(currentPosition.toLong()),
        color = TextSecondary,
        fontSize = 11.sp
      )

      Slider(
        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
        onValueChange = { fraction ->
          val targetPos = (fraction * duration).toInt()
          currentPosition = targetPos
          mediaPlayer?.seekTo(targetPos)
        },
        colors = SliderDefaults.colors(
          thumbColor = OverlayCyan,
          activeTrackColor = OverlayCyan,
          inactiveTrackColor = CameraBorder
        ),
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp)
      )

      Text(
        text = MediaUtils.formatDurationMs(duration.toLong()),
        color = TextSecondary,
        fontSize = 11.sp
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action button: Use this recording as overlay loop
    Button(
      onClick = onUseAsOverlay,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = OverlayCyan,
        contentColor = CameraBlack
      ),
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Use As Reference Loop Overlay",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
