package com.example.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RecordedVideo
import com.example.model.RecordingStatus
import com.example.ui.components.CameraBottomControls
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CameraViewFinder
import com.example.ui.components.OverlaySettingsSheet
import com.example.ui.components.PermissionRequestView
import com.example.ui.components.RecordingTimerBadge
import com.example.ui.components.VideoOverlayPlayer
import com.example.ui.theme.CameraBlack
import com.example.viewmodel.CameraViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(
  viewModel: CameraViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val cameraUiState by viewModel.cameraUiState.collectAsStateWithLifecycle()
  val overlayState by viewModel.overlayState.collectAsStateWithLifecycle()
  val isOverlaySheetOpen by viewModel.isOverlaySheetOpen.collectAsStateWithLifecycle()

  var showGrid by remember { mutableStateOf(true) }
  var videoCaptureInstance by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
  var cameraControlInstance by remember { mutableStateOf<CameraControl?>(null) }
  var activeRecording by remember { mutableStateOf<Recording?>(null) }

  // Check initial permissions
  val requiredPermissions = remember {
    val list = mutableListOf(
      Manifest.permission.CAMERA,
      Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      list.add(Manifest.permission.READ_MEDIA_VIDEO)
    } else {
      list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    list.toTypedArray()
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val cameraGranted = perms[Manifest.permission.CAMERA] == true
    val audioGranted = perms[Manifest.permission.RECORD_AUDIO] == true
    val hasAll = cameraGranted && audioGranted
    viewModel.setPermissionsGranted(hasAll)
  }

  // Gallery Reference Video Picker Launcher
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.setOverlayVideo(uri, context)
      Toast.makeText(context, "Overlay reference video imported!", Toast.LENGTH_SHORT).show()
    }
  }

  // Check permission state on start
  LaunchedEffect(Unit) {
    val cameraGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val audioGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    viewModel.setPermissionsGranted(cameraGranted && audioGranted)
  }

  // Handle errors
  LaunchedEffect(cameraUiState.errorMessage) {
    cameraUiState.errorMessage?.let { error ->
      snackbarHostState.showSnackbar(error)
      viewModel.clearErrorMessage()
    }
  }

  // Stop recording safely if disposed
  DisposableEffect(Unit) {
    onDispose {
      try {
        activeRecording?.stop()
        activeRecording?.close()
      } catch (_: Exception) {}
    }
  }

  if (!cameraUiState.hasPermissions) {
    PermissionRequestView(
      onRequestPermissions = {
        permissionLauncher.launch(requiredPermissions)
      },
      modifier = modifier
    )
    return
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = CameraBlack,
    modifier = modifier.fillMaxSize()
  ) { _ ->
    Box(modifier = Modifier.fillMaxSize()) {
      // 1. Camera View Finder (CameraX Preview)
      CameraViewFinder(
        isBackCamera = cameraUiState.isBackCamera,
        isTorchOn = cameraUiState.isTorchOn,
        showGrid = showGrid,
        onVideoCaptureReady = { capture, control ->
          videoCaptureInstance = capture
          cameraControlInstance = control
          viewModel.setCameraInitialized(true)
        },
        onZoomRangeChanged = { min, max ->
          viewModel.setZoomRange(min, max)
        },
        onZoomRatioChanged = { ratio ->
          viewModel.updateZoomRatio(ratio)
        },
        modifier = Modifier.fillMaxSize()
      )

      // 2. Video Overlay (The key requested feature: Looping video without audio from gallery)
      VideoOverlayPlayer(
        overlayState = overlayState,
        onUpdateOpacity = { viewModel.updateOverlayOpacity(it) },
        onUpdateScale = { viewModel.updateOverlayScale(it) },
        onUpdateOffset = { dx, dy -> viewModel.updateOverlayOffset(dx, dy) },
        onResetTransform = { viewModel.resetOverlayTransform() },
        onToggleLock = { viewModel.toggleOverlayLock() },
        onToggleVisibility = { viewModel.toggleOverlayVisibility() },
        onToggleControls = { viewModel.toggleOverlayControls() },
        onOpenSettings = { viewModel.openOverlaySheet() },
        onClose = { viewModel.clearOverlayVideo() },
        modifier = Modifier.fillMaxSize()
      )

      // 3. Top Action Bar (Torch, Audio Toggle)
      CameraTopBar(
        isTorchOn = cameraUiState.isTorchOn,
        isAudioRecordingEnabled = cameraUiState.isAudioRecordingEnabled,
        isBackCamera = cameraUiState.isBackCamera,
        overlayState = overlayState,
        recordingStatus = cameraUiState.recordingStatus,
        onToggleTorch = { viewModel.toggleTorch() },
        onToggleAudioRecording = { viewModel.toggleAudioRecording() },
        onOpenOverlaySettings = { viewModel.openOverlaySheet() },
        modifier = Modifier
          .align(Alignment.TopCenter)
          .statusBarsPadding()
      )

      // 4. Recording Timer Badge (Center Top)
      AnimatedVisibility(
        visible = cameraUiState.recordingStatus == RecordingStatus.RECORDING ||
          cameraUiState.recordingStatus == RecordingStatus.PAUSED,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = Modifier
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(top = 64.dp)
      ) {
        RecordingTimerBadge(
          durationSeconds = cameraUiState.recordingDurationSeconds,
          recordingStatus = cameraUiState.recordingStatus
        )
      }

      // 5. Bottom Controls (Shutter, Camera Switch, Import Overlay Video)
      CameraBottomControls(
        recordingStatus = cameraUiState.recordingStatus,
        overlayState = overlayState,
        onRecordClick = {
          handleRecordClick(
            context = context,
            videoCapture = videoCaptureInstance,
            activeRecording = activeRecording,
            cameraUiState = cameraUiState,
            viewModel = viewModel,
            onActiveRecordingChanged = { activeRecording = it }
          )
        },
        onPauseResumeClick = {
          handlePauseResumeClick(
            activeRecording = activeRecording,
            recordingStatus = cameraUiState.recordingStatus,
            viewModel = viewModel
          )
        },
        onPickOverlayVideo = {
          videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
          )
        },
        onOpenOverlaySettings = { viewModel.openOverlaySheet() },
        onToggleCameraLens = { viewModel.toggleCameraLens() },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
      )
    }

    // Modal Sheet: Overlay Configuration (Presets, Opacity, Speed, etc.)
    if (isOverlaySheetOpen && overlayState.uri != null) {
      OverlaySettingsSheet(
        overlayState = overlayState,
        onDismiss = { viewModel.closeOverlaySheet() },
        onSelectPreset = { viewModel.setOverlayPreset(it) },
        onUpdateOpacity = { viewModel.updateOverlayOpacity(it) },
        onUpdateScale = { viewModel.updateOverlayScale(it) },
        onUpdateSpeed = { viewModel.setOverlayPlaybackSpeed(it) },
        onResetPosition = { viewModel.resetOverlayTransform() },
        onToggleLock = { viewModel.toggleOverlayLock() },
        onToggleVisibility = { viewModel.toggleOverlayVisibility() },
        onReplaceVideo = {
          videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
          )
        },
        onRemoveOverlay = { viewModel.clearOverlayVideo() }
      )
    }
  }
}

private fun handleRecordClick(
  context: Context,
  videoCapture: VideoCapture<Recorder>?,
  activeRecording: Recording?,
  cameraUiState: com.example.model.CameraRecordingUiState,
  viewModel: CameraViewModel,
  onActiveRecordingChanged: (Recording?) -> Unit
) {
  if (cameraUiState.recordingStatus == RecordingStatus.RECORDING ||
    cameraUiState.recordingStatus == RecordingStatus.PAUSED
  ) {
    // Stop recording
    try {
      activeRecording?.stop()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return
  }

  // Start new recording
  val capture = videoCapture ?: run {
    Toast.makeText(context, "Camera not ready for recording", Toast.LENGTH_SHORT).show()
    return
  }

  val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
  val fileName = "ShyCam_$timeStamp.mp4"

  val contentValues = ContentValues().apply {
    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ShyCam")
    }
  }

  val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
    context.contentResolver,
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
  ).setContentValues(contentValues).build()

  var pendingRecording = capture.output
    .prepareRecording(context, mediaStoreOutputOptions)

  // Enable audio recording if permission is granted and toggle is enabled
  if (cameraUiState.isAudioRecordingEnabled &&
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
  ) {
    pendingRecording = pendingRecording.withAudioEnabled()
  }

  viewModel.startRecordingTimer()

  val recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
    when (recordEvent) {
      is VideoRecordEvent.Start -> {
        // Recording actively rolling
      }
      is VideoRecordEvent.Pause -> {
        viewModel.pauseRecordingTimer()
      }
      is VideoRecordEvent.Resume -> {
        viewModel.resumeRecordingTimer()
      }
      is VideoRecordEvent.Finalize -> {
        onActiveRecordingChanged(null)
        if (!recordEvent.hasError()) {
          val outputUri = recordEvent.outputResults.outputUri
          val recorded = RecordedVideo(
            id = System.currentTimeMillis(),
            uri = outputUri,
            title = fileName,
            durationMs = cameraUiState.recordingDurationSeconds * 1000L,
            sizeBytes = 0L,
            dateAdded = System.currentTimeMillis()
          )
          viewModel.stopRecordingTimer(recorded)
          Toast.makeText(context, "Video saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
          viewModel.setRecordingError("Recording failed: ${recordEvent.cause?.message ?: "Unknown error"}")
        }
      }
    }
  }

  onActiveRecordingChanged(recording)
}

private fun handlePauseResumeClick(
  activeRecording: Recording?,
  recordingStatus: RecordingStatus,
  viewModel: CameraViewModel
) {
  val recording = activeRecording ?: return
  try {
    if (recordingStatus == RecordingStatus.RECORDING) {
      recording.pause()
      viewModel.pauseRecordingTimer()
    } else if (recordingStatus == RecordingStatus.PAUSED) {
      recording.resume()
      viewModel.resumeRecordingTimer()
    }
  } catch (_: Exception) {}
}
