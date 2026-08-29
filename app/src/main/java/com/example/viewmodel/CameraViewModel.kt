package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CameraRecordingUiState
import com.example.model.OverlayPreset
import com.example.model.OverlayVideoState
import com.example.model.RecordedVideo
import com.example.model.RecordingStatus
import com.example.util.MediaUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

  private val _cameraUiState = MutableStateFlow(CameraRecordingUiState())
  val cameraUiState: StateFlow<CameraRecordingUiState> = _cameraUiState.asStateFlow()

  private val _overlayState = MutableStateFlow(OverlayVideoState())
  val overlayState: StateFlow<OverlayVideoState> = _overlayState.asStateFlow()

  private val _selectedPreviewVideo = MutableStateFlow<RecordedVideo?>(null)
  val selectedPreviewVideo: StateFlow<RecordedVideo?> = _selectedPreviewVideo.asStateFlow()

  private val _isGallerySheetOpen = MutableStateFlow(false)
  val isGallerySheetOpen: StateFlow<Boolean> = _isGallerySheetOpen.asStateFlow()

  private val _isOverlaySheetOpen = MutableStateFlow(false)
  val isOverlaySheetOpen: StateFlow<Boolean> = _isOverlaySheetOpen.asStateFlow()

  private var timerJob: Job? = null

  init {
    loadRecordedVideos()
  }

  fun setPermissionsGranted(granted: Boolean) {
    _cameraUiState.update { it.copy(hasPermissions = granted) }
    if (granted) {
      loadRecordedVideos()
    }
  }

  fun setCameraInitialized(initialized: Boolean) {
    _cameraUiState.update { it.copy(isCameraInitialized = initialized) }
  }

  fun toggleCameraLens() {
    _cameraUiState.update { it.copy(isBackCamera = !it.isBackCamera, isTorchOn = false) }
  }

  fun toggleTorch() {
    _cameraUiState.update { it.copy(isTorchOn = !it.isTorchOn) }
  }

  fun toggleAudioRecording() {
    _cameraUiState.update { it.copy(isAudioRecordingEnabled = !it.isAudioRecordingEnabled) }
  }

  fun updateZoomRatio(ratio: Float) {
    _cameraUiState.update {
      it.copy(zoomRatio = ratio.coerceIn(it.minZoomRatio, it.maxZoomRatio))
    }
  }

  fun setZoomRange(min: Float, max: Float) {
    _cameraUiState.update {
      it.copy(
        minZoomRatio = min,
        maxZoomRatio = max,
        zoomRatio = it.zoomRatio.coerceIn(min, max)
      )
    }
  }

  fun startRecordingTimer() {
    _cameraUiState.update {
      it.copy(
        recordingStatus = RecordingStatus.RECORDING,
        recordingDurationSeconds = 0L,
        errorMessage = null
      )
    }
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000L)
        _cameraUiState.update {
          if (it.recordingStatus == RecordingStatus.RECORDING) {
            it.copy(recordingDurationSeconds = it.recordingDurationSeconds + 1L)
          } else {
            it
          }
        }
      }
    }
  }

  fun pauseRecordingTimer() {
    _cameraUiState.update { it.copy(recordingStatus = RecordingStatus.PAUSED) }
  }

  fun resumeRecordingTimer() {
    _cameraUiState.update { it.copy(recordingStatus = RecordingStatus.RECORDING) }
  }

  fun stopRecordingTimer(outputVideo: RecordedVideo?) {
    timerJob?.cancel()
    timerJob = null
    _cameraUiState.update {
      it.copy(
        recordingStatus = RecordingStatus.IDLE,
        lastRecordedVideo = outputVideo ?: it.lastRecordedVideo
      )
    }
    if (outputVideo != null) {
      _selectedPreviewVideo.value = outputVideo
    }
    loadRecordedVideos()
  }

  fun setRecordingError(message: String) {
    timerJob?.cancel()
    timerJob = null
    _cameraUiState.update {
      it.copy(
        recordingStatus = RecordingStatus.IDLE,
        errorMessage = message
      )
    }
  }

  fun clearErrorMessage() {
    _cameraUiState.update { it.copy(errorMessage = null) }
  }

  // --- Overlay Management ---

  fun setOverlayVideo(uri: Uri, context: Context) {
    val title = MediaUtils.getFileName(context, uri)
    _overlayState.update {
      it.copy(
        uri = uri,
        title = title,
        isVisible = true,
        isControlsExpanded = false
      )
    }
  }

  fun updateOverlayOpacity(opacity: Float) {
    _overlayState.update { it.copy(opacity = opacity.coerceIn(0.1f, 1.0f)) }
  }

  fun updateOverlayScale(scale: Float) {
    _overlayState.update { it.copy(scale = scale.coerceIn(0.3f, 2.0f)) }
  }

  fun updateOverlayOffset(dx: Float, dy: Float) {
    _overlayState.update {
      it.copy(
        offsetX = it.offsetX + dx,
        offsetY = it.offsetY + dy
      )
    }
  }

  fun resetOverlayTransform() {
    _overlayState.update {
      it.copy(
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f
      )
    }
  }

  fun setOverlayPreset(preset: OverlayPreset) {
    _overlayState.update { current ->
      val prev = if (current.preset != OverlayPreset.FULL_SCREEN) current.preset else current.previousPreset
      when (preset) {
        OverlayPreset.PIP_CORNER -> current.copy(
          preset = preset,
          previousPreset = prev,
          scale = 0.85f,
          opacity = 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
        OverlayPreset.GHOST_GUIDE -> current.copy(
          preset = preset,
          previousPreset = prev,
          scale = 1.0f,
          opacity = 0.45f,
          offsetX = 0f,
          offsetY = 0f
        )
        OverlayPreset.SPLIT_TOP -> current.copy(
          preset = preset,
          previousPreset = prev,
          scale = 1.0f,
          opacity = 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
        OverlayPreset.SPLIT_BOTTOM -> current.copy(
          preset = preset,
          previousPreset = prev,
          scale = 1.0f,
          opacity = 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
        OverlayPreset.FULL_SCREEN -> current.copy(
          preset = preset,
          previousPreset = prev,
          scale = 1.0f,
          opacity = 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
      }
    }
  }

  fun toggleFullscreenOverlay() {
    _overlayState.update { current ->
      if (current.preset == OverlayPreset.FULL_SCREEN) {
        val target = if (current.previousPreset == OverlayPreset.FULL_SCREEN) OverlayPreset.PIP_CORNER else current.previousPreset
        current.copy(
          preset = target,
          scale = if (target == OverlayPreset.PIP_CORNER) 0.85f else 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
      } else {
        current.copy(
          previousPreset = current.preset,
          preset = OverlayPreset.FULL_SCREEN,
          scale = 1.0f,
          opacity = 1.0f,
          offsetX = 0f,
          offsetY = 0f
        )
      }
    }
  }

  fun setOverlayPlaybackSpeed(speed: Float) {
    _overlayState.update { it.copy(playbackSpeed = speed) }
  }

  fun toggleOverlayVisibility() {
    _overlayState.update { it.copy(isVisible = !it.isVisible) }
  }

  fun toggleOverlayLock() {
    _overlayState.update { it.copy(isLocked = !it.isLocked) }
  }

  fun toggleOverlayControls() {
    _overlayState.update { it.copy(isControlsExpanded = !it.isControlsExpanded) }
  }

  fun clearOverlayVideo() {
    _overlayState.update {
      OverlayVideoState() // reset
    }
  }

  // --- Gallery & Library Sheet ---

  fun openGallerySheet() {
    loadRecordedVideos()
    _isGallerySheetOpen.value = true
  }

  fun closeGallerySheet() {
    _isGallerySheetOpen.value = false
  }

  fun openOverlaySheet() {
    _isOverlaySheetOpen.value = true
  }

  fun closeOverlaySheet() {
    _isOverlaySheetOpen.value = false
  }

  fun setPreviewVideo(video: RecordedVideo?) {
    _selectedPreviewVideo.value = video
  }

  fun loadRecordedVideos() {
    viewModelScope.launch {
      try {
        val videos = MediaUtils.queryAppVideos(getApplication())
        _cameraUiState.update { it.copy(recordedVideos = videos) }
      } catch (_: Exception) {
      }
    }
  }

  fun deleteRecordedVideo(video: RecordedVideo) {
    viewModelScope.launch {
      try {
        getApplication<Application>().contentResolver.delete(video.uri, null, null)
        loadRecordedVideos()
        if (_selectedPreviewVideo.value?.id == video.id) {
          _selectedPreviewVideo.value = null
        }
      } catch (_: Exception) {
      }
    }
  }
}
